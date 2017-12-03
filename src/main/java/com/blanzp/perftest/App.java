package com.blanzp.perftest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class App {

    // http://localhost:8080/hello-world
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}