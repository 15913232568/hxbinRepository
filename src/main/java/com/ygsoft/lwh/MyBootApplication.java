package com.ygsoft.lwh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = {"com.ygsoft.lwh"})
@Configuration
@EnableAsync
public class MyBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBootApplication.class,args);
    }

}
