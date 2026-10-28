package ru.sber.transport.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestChannelBinderConfiguration.class)
public class DoNotStartThis {

    public static void main(String[] args) {
        SpringApplication.run(DoNotStartThis.class, args);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
