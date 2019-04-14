package fr.lmarchau.proto.bcm.data.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Flight {

    private double price;
    private String id;
    private String departure;
    private String arrival;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    @JsonProperty("flight")
    private void unpackNestedFlight(Map<String, String> flight) {
        log.debug("Unpack inner flight {}", flight);
        id = flight.get("id");
        departure = flight.get("departure_airport");
        arrival = flight.get("arrival_airport");
        departureAt = LocalDateTime.parse(flight.get("departure_time"));
        arrivalAt = LocalDateTime.parse(flight.get("arrival_time"));
    }

    @JsonProperty("legs")
    private void unpackNestedLegs(List<Map<String, String>> legs) {
        log.debug("Unpack inner legs {}", legs);
        id = (String) legs.get(0).get("id");
        departure = (String) legs.get(0).get("departure_airport");
        arrival = (String) legs.get(0).get("arrival_airport");
        departureAt = LocalDateTime.parse(legs.get(0).get("departure_time"));
        arrivalAt = LocalDateTime.parse(legs.get(0).get("arrival_time"));
    }
}
