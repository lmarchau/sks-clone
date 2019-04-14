package fr.lmarchau.proto.bcm.service;

import fr.lmarchau.proto.bcm.data.Group;
import fr.lmarchau.proto.bcm.data.Results;
import fr.lmarchau.proto.bcm.data.enums.TripType;
import fr.lmarchau.proto.bcm.data.external.Flight;
import fr.lmarchau.proto.bcm.repository.FlightRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    @Mock
    private FlightRepository airMoonRepository;
    @Mock
    private FlightRepository airJazzRepository;

    @InjectMocks
    private SearchService searchService = new SearchService(airMoonRepository, airJazzRepository);

    LocalDate now = LocalDate.now();
    LocalDateTime midnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

    @Test
    public void searchShouldReturnSearchParameters() {
        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.OW);
        assertThat(results)
                .isNotNull()
                .extracting("departure", "arrival", "departureAt", "returnAt", "tripType").containsOnly("DEP", "ARR", now, now.plusDays(2), TripType.OW);
    }

    @Test
    public void searchOWTripShouldOnlySearchOW() {
        searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.OW);
        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("DEP", "ARR", now);
    }

    @Test
    public void searchReturnTripShouldSearchOWAndReturnOW() {
        searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.R);
        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airJazzRepository).search("ARR", "DEP", now.plusDays(2));
        verify(airMoonRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("ARR", "DEP", now.plusDays(2));
    }

    @Test
    public void searchOWTripShouldOnlyReturnOWCombinaison() {
        when(airJazzRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("1").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("12").build()
                ));
        when(airMoonRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("123").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("1234").build()
                ));

        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.OW);

        assertThat(results.getGroups().stream().map(Group::getCombinations).flatMap(l -> l.stream()).collect(toList())).extracting("departureId").doesNotContainNull().containsOnly("1", "12", "123", "1234");
        assertThat(results.getGroups().stream().map(Group::getCombinations).flatMap(l -> l.stream()).collect(toList())).extracting("returnId").containsOnlyNulls();

        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("DEP", "ARR", now);
    }

    @Test
    public void searchReturnTripShouldOnlyReturnCombinaison() {
        when(airJazzRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("D1").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("D12").build()
                ));
        when(airJazzRepository.search("ARR", "DEP", now.plusDays(2))).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(6)).arrivalAt(midnight.plusHours(7)).id("R1").build()
                ));
        when(airMoonRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("D123").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("D1234").build()
                ));
        when(airMoonRepository.search("ARR", "DEP", now.plusDays(2))).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("R123").build(),
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("R1234").build()
                ));

        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.R);

        assertThat(results.getGroups().stream().map(Group::getCombinations).flatMap(l -> l.stream()).collect(toList())).extracting("departureId").doesNotContainNull().containsOnly("D1", "D123");
        assertThat(results.getGroups().stream().map(Group::getCombinations).flatMap(l -> l.stream()).collect(toList())).extracting("returnId").doesNotContainNull().containsOnly("R1");

        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airJazzRepository).search("ARR", "DEP", now.plusDays(2));
        verify(airMoonRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("ARR", "DEP", now.plusDays(2));
    }

    @Test
    public void searchOWTripShouldSortByPrice() {
        when(airJazzRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("1").price(1).build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("12").price(2).build()
                ));
        when(airMoonRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("123").price(1).build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("1234").price(3).build()
                ));

        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.OW);

        assertThat(results.getGroups()).extracting("price").containsExactly(1d, 2d, 3d);

        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("DEP", "ARR", now);
    }

    @Test
    public void searchOWTripShouldContainsAllFlightDetails() {
        when(airJazzRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("1").price(1).build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("12").price(2).build()
                ));
        when(airMoonRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("123").price(1).build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("1234").price(3).build()
                ));

        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.OW);

        assertThat(results.getFlights()).extracting("id").containsOnly("1", "12", "123", "1234");

        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("DEP", "ARR", now);
    }

    @Test
    public void searchReturnTripShouldOnlyReturnFlightDetails() {
        when(airJazzRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("D1").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("D12").build()
                ));
        when(airJazzRepository.search("ARR", "DEP", now.plusDays(2))).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(6)).arrivalAt(midnight.plusHours(7)).id("R1").build()
                ));
        when(airMoonRepository.search("DEP", "ARR", now)).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("D123").build(),
                        Flight.builder().departure("DEP").arrival("ARR").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("D1234").build()
                ));
        when(airMoonRepository.search("ARR", "DEP", now.plusDays(2))).thenReturn(
                Arrays.asList(
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(4)).arrivalAt(midnight.plusHours(5)).id("R123").build(),
                        Flight.builder().departure("ARR").arrival("DEP").departureAt(midnight.plusHours(5)).arrivalAt(midnight.plusHours(6)).id("R1234").build()
                ));

        Results results = searchService.search("DEP", "ARR", now, now.plusDays(2), TripType.R);

        assertThat(results.getFlights()).extracting("id").containsOnly("D1", "D123", "R1");

        verify(airJazzRepository).search("DEP", "ARR", now);
        verify(airJazzRepository).search("ARR", "DEP", now.plusDays(2));
        verify(airMoonRepository).search("DEP", "ARR", now);
        verify(airMoonRepository).search("ARR", "DEP", now.plusDays(2));
    }

}
