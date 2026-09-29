package ru.sber.transport.authorization;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.aspect.CheckOrganizationAspect;
import ru.sber.transport.authorization.service.CheckUserAccessService;

/**
 * Configure authorization components.
 */
@Configuration
@ComponentScan
public class AuthorizationConfiguration {

    @Bean
    public CheckOrganizationAspect checkOrganizationAspect(ObjectProvider<CheckUserAccessService> service) {
        return new CheckOrganizationAspect(service);
    }

}
