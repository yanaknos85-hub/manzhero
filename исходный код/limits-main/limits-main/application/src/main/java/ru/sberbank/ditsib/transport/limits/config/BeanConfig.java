package ru.sberbank.ditsib.transport.limits.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;

import java.time.Clock;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1452")
@Configuration
public class BeanConfig {
    
    @Bean
    Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingServicePerPeriodBeans(
            Collection<LimitSharingPerPeriodService<? extends Period>> beanList
                                                                                                           ) {
        return beanList.stream()
                       .flatMap(service -> service.types().stream().map(type -> new AbstractMap.SimpleEntry<>(type, service)))
                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
    
}
