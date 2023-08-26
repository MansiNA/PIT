package com.example.application;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());

            logger.info("Hier könnten Daten geladen werden......");


        };
    }
}
