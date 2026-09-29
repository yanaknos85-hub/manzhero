package ru.sber.transport.tariff.external;

import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Главный класс приложения.
 */
@NoArgsConstructor
@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки.
     */
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

}
