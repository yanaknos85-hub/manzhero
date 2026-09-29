package ru.sber.transport.integrations.config.feign;

import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import ru.sberbank.ditsib.converters.ZonedDateTimeDeserializer;

/**
 * Конфиг сериализаторов/дезериализаторов feign
 */
@Configuration
public class FeignFormatterRegister implements FeignFormatterRegistrar {
    
    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addFormatter(new ZonedDateTimeDeserializer());
    }
}
