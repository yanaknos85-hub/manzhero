package ru.sberbank.ditsib.transport.limits.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;
import ru.sberbank.ditsib.transport.limits.human_readable_id.dao.CompanySQRepositoryLimits;

@Configuration
public class SqConfigLimits {
    
    @Bean(name = "sQGeneratorLimits")
    public SQGenerator sQGenerator(
            CompanySQService sqService, CompanySQRepositoryLimits companySQRepositoryLimits,
            HumanReadbaleIdFormatter humanReadbaleIdFormatter
                                  ) {
        return new SQGeneratorImpl(sqService, companySQRepositoryLimits, humanReadbaleIdFormatter);
    }
    
}
