package com.example.meetingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
public class MeetingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingAppApplication.class, args);
    }

    //todo добавить Circuit Breaker (Resilience4j)
    //todo добавить DLQ
    //todo Distributed tracing
    //todo заменить лонги в первичных ключах на UUID

}
