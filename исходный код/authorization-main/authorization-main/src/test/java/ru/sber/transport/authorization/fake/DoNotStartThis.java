package ru.sber.transport.authorization.fake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.sber.transport.authorization.fake.controller.*;

/**
 * Тестовый апп. Библиотека не должна иметь контекста, она должна встраиваться существующий. Этот апп призван создать
 * тестовый контекст для проверки библиотеки.
 */
@SpringBootApplication
@Import({Controller.class, ForeignController.class, IssuedController.class, SubController.class, SkipUrlController.class})
public class DoNotStartThis {
    
    public static void main(String[] args) {
        SpringApplication.run(DoNotStartThis.class, args);
    }
    
}
