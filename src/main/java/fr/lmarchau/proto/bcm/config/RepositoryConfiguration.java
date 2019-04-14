package fr.lmarchau.proto.bcm.config;

import fr.lmarchau.proto.bcm.repository.FlightRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public FlightRepository airJazzRepository(RestTemplateBuilder builder) {
        return new FlightRepository(builder, "http://flights.beta.bcmenergy.fr/jazz/flights");
    }

    @Bean
    public FlightRepository airMoonRepository(RestTemplateBuilder builder) {
        return new FlightRepository(builder, "http://flights.beta.bcmenergy.fr/moon/flights");
    }

}
