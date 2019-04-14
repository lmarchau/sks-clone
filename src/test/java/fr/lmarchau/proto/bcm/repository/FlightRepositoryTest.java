package fr.lmarchau.proto.bcm.repository;

import fr.lmarchau.proto.bcm.config.RepositoryTestConfiguration;
import fr.lmarchau.proto.bcm.data.external.Flight;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@Import(RepositoryTestConfiguration.class)
@RestClientTest(FlightRepository.class)
public class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void searchMoonShouldReturnSomeResult() {
        server.expect(requestTo("http://test.tm/moon/flights?departure_date=2019-05-21&departure_airport=CDG&arrival_airport=LHR"))
                .andRespond(withSuccess()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(new ClassPathResource("dataset/air-moon.json")));

        List<Flight> result = flightRepository.search("CDG", "LHR", LocalDate.parse("2019-05-21"));
        assertThat(result).hasSize(3);
        assertThat(result).extracting("departure").containsOnly("CDG");
        assertThat(result).extracting("arrival").containsOnly("LHR");
        assertThat(result.stream().map(Flight::getDepartureAt).allMatch(d -> LocalDate.parse("2019-05-21").isEqual(d.toLocalDate()))).isTrue();
    }

    @Test
    public void searchJazzShouldReturnSomeResult() {
        server.expect(requestTo("http://test.tm/moon/flights?departure_date=2019-05-21&departure_airport=CDG&arrival_airport=LHR"))
                .andRespond(withSuccess()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(new ClassPathResource("dataset/air-moon.json")));

        List<Flight> result = flightRepository.search("CDG", "LHR", LocalDate.parse("2019-05-21"));
        assertThat(result).hasSize(3);
        assertThat(result).extracting("departure").containsOnly("CDG");
        assertThat(result).extracting("arrival").containsOnly("LHR");
        assertThat(result.stream().map(Flight::getDepartureAt).allMatch(d -> LocalDate.parse("2019-05-21").isEqual(d.toLocalDate()))).isTrue();
    }

    @Test
    public void searchWithAnAPIErrorShouldReturnEmptyResult() {
        server.expect(requestTo("http://test.tm/moon/flights?departure_date=2019-05-21&departure_airport=CDG&arrival_airport=LHR"))
                .andRespond(withServerError());

        List<Flight> result = flightRepository.search("CDG", "LHR", LocalDate.parse("2019-05-21"));
        assertThat(result).isEmpty();
    }

    @Test
    public void searchWithAnAPIUnexpectedResonseShouldReturnEmptyResult() {
        server.expect(requestTo("http://test.tm/moon/flights?departure_date=2019-05-21&departure_airport=CDG&arrival_airport=LHR"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        List<Flight> result = flightRepository.search("CDG", "LHR", LocalDate.parse("2019-05-21"));
        assertThat(result).isEmpty();
    }

}
