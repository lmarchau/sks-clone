package fr.lmarchau.proto.bcm.repository;

import fr.lmarchau.proto.bcm.data.external.Flight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Slf4j
public class FlightRepository {

    private static final String PARAMS = "?departure_date={departure_date}&departure_airport={departure_airport}&arrival_airport={arrival_airport}";

    private RestTemplate restTemplate;
    private String api;

    public FlightRepository(RestTemplateBuilder restTemplateBuilder, String api) {
        this.restTemplate = restTemplateBuilder.build();
        this.api = api + PARAMS;
    }

    public List<Flight> search(String departure, String arrival, LocalDate departureAt) {
        Map<String, Object> params = new HashMap<>();
        params.put("departure_airport", departure);
        params.put("arrival_airport", arrival);
        params.put("departure_date", departureAt);
        log.info("Call api {} with params {}", api, params);
        ResponseEntity<Flight[]> response = null;
        try {
             response = restTemplate.getForEntity(api, Flight[].class, params);
        } catch (RestClientException e) {
            log.error("Unexpected error from Jazz API", e);
            return Collections.emptyList();
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Unexpected response from Jazz API {}", response.getStatusCode(), response.getBody());
            return Collections.emptyList();
        }
        return Arrays.asList(response.getBody());
    }

}
