package ru.sberbank.ditsib.transport.limits.config;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee_;

import java.util.UUID;

/**
 * Конфигурация пути получения организации пользователя для проверки принадлежности организации.
 */
@Configuration
public class CheckAccessConfiguration {

    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
        EntityManager entityManager
    ) {
        return authenticated -> {
            var cb = entityManager.getCriteriaBuilder();
            var q = cb.createQuery(UUID.class);
            var employee = q.from(Employee.class);
            var predicate = cb.equal(employee.get(Employee_.USER_ID), authenticated);
            q = q.where(predicate);
            q = q.select(employee.get(Employee_.ORGANIZATION_ID));
            return entityManager.createQuery(q).getResultList().stream().findFirst().orElse(null);
        };
    }

}
