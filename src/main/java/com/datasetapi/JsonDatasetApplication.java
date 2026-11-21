package com.datasetapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;
@SpringBootApplication
public class JsonDatasetApplication {
    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // Set default timezone to UTC
        SpringApplication.run(JsonDatasetApplication.class, args);
    }
}
