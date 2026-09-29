package ru.sberbank.ditsib;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.sberbank.ditsib.configuration.AdditionalPropertiesLoader;

import java.lang.annotation.*;


/**
 * Main annotation of microservice.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication
@Import(AdditionalPropertiesLoader.class)
public @interface Microservice {
}
