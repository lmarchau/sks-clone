package fr.lmarchau.proto.bcm.service;

import fr.lmarchau.proto.bcm.data.Combination;
import fr.lmarchau.proto.bcm.data.Group;
import fr.lmarchau.proto.bcm.data.Results;
import fr.lmarchau.proto.bcm.data.enums.TripType;
import fr.lmarchau.proto.bcm.data.external.Flight;
import fr.lmarchau.proto.bcm.data.internal.FlightTuple;
import fr.lmarchau.proto.bcm.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class SearchService {

    private FlightRepository airMoonRepository;
    private FlightRepository airJazzRepository;

    public SearchService(FlightRepository airMoonRepository, FlightRepository airJazzRepository) {
        this.airMoonRepository = airMoonRepository;
        this.airJazzRepository = airJazzRepository;
    }

    // FIXME split this code
    public Results search(String departure, String arrival, LocalDate departureAt, LocalDate returnAt, TripType tripType) {
        Results results = Results.builder().departure(departure).arrival(arrival).departureAt(departureAt).returnAt(returnAt).tripType(tripType).build();
        List<Flight> ow = new ArrayList<>();
        //search
        ow.addAll(airMoonRepository.search(departure, arrival, departureAt));
        ow.addAll(airJazzRepository.search(departure, arrival, departureAt));
        List<Flight> returns = new ArrayList<>();
        if (TripType.R == tripType) {
            returns.addAll(airMoonRepository.search(arrival, departure, returnAt));
            returns.addAll(airJazzRepository.search(arrival, departure, returnAt));
        }

        // AGGREGATE
        switch (tripType) {
            case OW:
                log.info("Group OW flights {}", ow);
                results.setGroups(ow.stream()
                        .collect(groupingBy(Flight::getPrice))
                        .entrySet()
                        .stream()
                        .map(e -> Group.builder()
                                .price(e.getKey())
                                .combinations(e.getValue().stream()
                                        .map(f -> Combination.builder().departureId(f.getId()).build())
                                        .collect(toList()))
                                .build())
                        .collect(toList()));
                results.setFlights(ow);
                break;

            case R:
                // FIXME Naive solution (lots of loops --> bad performance)
                log.info("Group OW / R flights {} / {}", ow, returns);
                Collections.sort(ow, Comparator.comparing(Flight::getDepartureAt));
                Collections.sort(returns, Comparator.comparing(Flight::getDepartureAt));
                // build combinaisons
                Map<Double, List<FlightTuple>> tuples = ow.stream()
                        .collect(toMap(Function.identity(), f ->
                                returns
                                        .stream()
                                       .filter(r -> f.getArrivalAt().isBefore(r.getDepartureAt())) // get all return flights after arrival date of current ow flight
                                       .collect(toList())))
                        .entrySet()
                        .stream()
                        .map(e -> e.getValue()
                                    .stream()
                                    .map(f -> FlightTuple.builder().ow(e.getKey()).back(f).price(e.getKey().getPrice() + f.getPrice()).build())
                                    .collect(toList())) // build all tuples with current ow flight
                        .flatMap(List::stream)
                        .collect(toList())
                        .stream()
                        .collect(groupingBy(FlightTuple::getPrice)); // group by price

                // build results
                results.setGroups(tuples.entrySet()
                                .stream()
                                .map(e -> Group.builder()
                                            .price(e.getKey())
                                            .combinations(e.getValue().stream()
                                                    .map(f -> Combination.builder().departureId(f.getOw().getId()).returnId(f.getBack().getId()).build())
                                                    .collect(toList()))
                                            .build())
                                .collect(toList()));
                // add only used flights to results
                results.setFlights(tuples.values().stream().flatMap(List::stream).map(ft -> Arrays.asList(ft.getOw(), ft.getBack())).flatMap(List::stream).distinct().collect(toList()));

        }

        // order results by price
        Collections.sort(results.getGroups(), Comparator.comparing(Group::getPrice));
        return results;
    }

}
