package ru.sber.transport.integrations.config;

import com.fasterxml.jackson.databind.Module;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SortJacksonModuleConfig {

    @Bean
    public Module sortJacksonModule() {
        return new SortJacksonModule();
    }

}
