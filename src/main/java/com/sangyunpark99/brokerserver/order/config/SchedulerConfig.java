package com.sangyunpark99.brokerserver.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulerConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService fillScheduler() {
        return Executors.newScheduledThreadPool(4);
    }
}
