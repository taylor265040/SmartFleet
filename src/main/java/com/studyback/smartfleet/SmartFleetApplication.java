package com.studyback.smartfleet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.studyback.smartfleet.mapper")
public class SmartFleetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFleetApplication.class, args);
    }

}
