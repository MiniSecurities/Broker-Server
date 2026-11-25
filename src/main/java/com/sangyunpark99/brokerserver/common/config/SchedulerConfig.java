package com.sangyunpark99.brokerserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService fillScheduler() {
        return Executors.newScheduledThreadPool(4);
    }
}
