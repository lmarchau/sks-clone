package fr.lmarchau.proto.bcm.web;

import fr.lmarchau.proto.bcm.data.Results;
import fr.lmarchau.proto.bcm.data.enums.TripType;
import fr.lmarchau.proto.bcm.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@Slf4j
public class FlightController {

    private SearchService searchService;

    public FlightController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/flights")
    public ResponseEntity<Results> search(@RequestParam(value = "departure_airport", required = false) String departure,
                                          @RequestParam(value = "arrival_airport", required = false) String arrival,
                                          @RequestParam(value = "departure_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
                                          @RequestParam(value = "return_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                          @RequestParam(value = "tripType", required = false) TripType tripType) {
        log.info("Flight search params: departure_airport {}, arrival_airport {}, departure_date {}, return_date {}, tripType {}", departure, arrival, departureDate, returnDate, tripType);
        if (Objects.isNull(departure) || Objects.isNull(arrival) || Objects.isNull(departureDate) || (Objects.isNull(returnDate) && TripType.R.equals(tripType)) || Objects.isNull(tripType)) {
            log.warn("Bad Parameters...");
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(searchService.search(departure, arrival, departureDate, returnDate, tripType));
        }
    }

}
