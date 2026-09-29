package ru.sberbank.ditsib.corpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.sberbank.ditsib.corpclient.human_readable_id.dao.CompanySQRepository;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;

/**
 * Конфиг необходим для того чтобы ApplicationContextProvider попал в контекст и получил ApplicationContext, который
 * нужен при инициализации валидатора
 */
@Configuration
public class SqConfig {

    @Bean
    @Primary
    SQGenerator sQGenerator(
            CompanySQService sqService, CompanySQRepository companySQRepositoryEmployee,
            HumanReadbaleIdFormatter humanReadbaleIdFormatter) {
        return new SQGeneratorImpl(sqService, companySQRepositoryEmployee, humanReadbaleIdFormatter);
    }
}
