package ru.sberbank.ditsib.corpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

/**
 * Конфигурация проверки доступа пользователя к изменению данных организации.
 */
@Configuration
public class CheckAccessConfiguration {
    
    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            EmployeeRepository employeeRepository
                                                        ) {
        return authenticated -> employeeRepository.findByUserId(authenticated)
                                                    .map(Employee::getOrganization)
                                                    .map(Organization::getId).orElse(null);
    }
    
}
