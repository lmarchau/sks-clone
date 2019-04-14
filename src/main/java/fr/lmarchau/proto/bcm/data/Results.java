package fr.lmarchau.proto.bcm.data;

import fr.lmarchau.proto.bcm.data.enums.TripType;
import fr.lmarchau.proto.bcm.data.external.Flight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Results {

    private String departure;
    private String arrival;
    private LocalDate departureAt;
    private LocalDate returnAt;
    private TripType tripType;

    private List<Flight> flights;
    private List<Group> groups;
}
