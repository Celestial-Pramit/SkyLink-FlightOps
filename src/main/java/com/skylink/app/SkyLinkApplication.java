package com.skylink.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkyLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyLinkApplication.class, args);
    }
}