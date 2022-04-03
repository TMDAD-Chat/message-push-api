package es.unizar.tmdad.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class PrometheusCustomMetricsConfiguration {

    @Bean
    public AtomicInteger onlineUsersGauge(MeterRegistry meterRegistry){
        return meterRegistry.gauge("users_online", new AtomicInteger(0));
    }

}
