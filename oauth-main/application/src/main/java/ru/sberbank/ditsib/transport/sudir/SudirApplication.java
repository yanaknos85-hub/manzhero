package ru.sberbank.ditsib.transport.sudir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.ZoneOffset;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableTransactionManagement
public class SudirApplication {

    /**
     * Точка входа.
     *
     * @param args аргументы.
     */
	public static void main(String... args) {
		SpringApplication.run(SudirApplication.class, args);
	}
    
    @Bean
    Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }

}
