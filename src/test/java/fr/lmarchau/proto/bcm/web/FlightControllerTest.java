package fr.lmarchau.proto.bcm.web;


import fr.lmarchau.proto.bcm.data.Combination;
import fr.lmarchau.proto.bcm.data.Group;
import fr.lmarchau.proto.bcm.data.Results;
import fr.lmarchau.proto.bcm.data.enums.TripType;
import fr.lmarchau.proto.bcm.data.external.Flight;
import fr.lmarchau.proto.bcm.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FlightControllerTest {

    @MockBean
    private SearchService searchService;

    @Autowired
    private MockMvc mvc;

    private LocalDate now = LocalDate.now();
    private LocalDateTime time = LocalDateTime.now();

    @Test
    public void searchShouldReturnSomeResult() throws Exception {

        given(searchService.search("ALL", "ALL", now, now.plusDays(2), TripType.OW))
                .willReturn(Results.builder()
                        .departure("ALL")
                        .arrival("ALL")
                        .departureAt(now)
                        .returnAt(now.plusDays(2))
                        .tripType(TripType.OW)
                        .flights(Arrays.asList(
                                Flight.builder().departure("ALL").arrival("ALL").departureAt(time).arrivalAt(time.plusHours(1)).price(10d).id("D").build(),
                                Flight.builder().departure("ALL").arrival("ALL").departureAt(time.plusHours(2)).arrivalAt(time.plusHours(3)).price(12d).id("R").build()))
                        .groups(Arrays.asList(Group.builder().price(22d).combinations(Arrays.asList(Combination.builder().departureId("D").returnId("R").build())).build()))
                        .build());

        mvc.perform(get("/flights")
                .param("departure_airport", "ALL")
                .param("arrival_airport", "ALL")
                .param("departure_date", now.toString())
                .param("return_date", now.plusDays(2).toString())
                .param("tripType", TripType.OW.name())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departure").value("ALL"))
                .andExpect(jsonPath("$.arrival").value("ALL"))
                .andExpect(jsonPath("$.departureAt").value(now.format(DateTimeFormatter.ISO_DATE)))
                .andExpect(jsonPath("$.returnAt").value(now.plusDays(2).format(DateTimeFormatter.ISO_DATE)))
                .andExpect(jsonPath("$.tripType").value(TripType.OW.name()))
                .andExpect(jsonPath("$.flights[0].departureAt").value(time.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andExpect(jsonPath("$.groups").isArray());

        verify(searchService).search("ALL", "ALL", now, now.plusDays(2), TripType.OW);

    }

    @Test
    public void searchShouldReturnBadRequest() throws Exception {

        mvc.perform(get("/flights")
                .param("departure_airport", "ALL")
                .param("arrival_airport", "ALL")
                .param("tripType", TripType.OW.name())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(searchService);

    }

}
