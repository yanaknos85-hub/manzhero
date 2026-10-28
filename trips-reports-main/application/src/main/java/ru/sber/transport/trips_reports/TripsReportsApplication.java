package ru.sber.transport.trips_reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Основной класс приложения.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TripsReportsApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы запуска.
     */
    public static void main(String... args) {
        SpringApplication.run(TripsReportsApplication.class, args);
    }

}
