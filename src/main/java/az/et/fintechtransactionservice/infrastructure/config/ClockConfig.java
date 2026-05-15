package az.et.fintechtransactionservice.infrastructure.config;

import az.et.fintechtransactionservice.application.port.out.ClockPort;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    public ClockPort clockPort() {
        return Instant::now;
    }
}

