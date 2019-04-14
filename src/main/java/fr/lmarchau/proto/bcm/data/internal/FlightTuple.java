package fr.lmarchau.proto.bcm.data.internal;

import fr.lmarchau.proto.bcm.data.external.Flight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlightTuple {

    private Flight ow;
    private Flight back;
    private double price;

}
