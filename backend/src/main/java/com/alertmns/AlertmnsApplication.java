package com.alertmns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlertmnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertmnsApplication.class, args);
    }
}
