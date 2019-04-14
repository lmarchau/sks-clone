package fr.lmarchau.proto.bcm.config;

import fr.lmarchau.proto.bcm.repository.FlightRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryTestConfiguration {

    @Bean
    public FlightRepository flightRepository(RestTemplateBuilder builder) {
        return new FlightRepository(builder, "http://test.tm/moon/flights");
    }

}
