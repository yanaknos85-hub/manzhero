package ru.sberbank.ditsib.transport.approvals.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.transport.approvals.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.approvals.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;

/**
 * Конфигурация пути получения организации пользователя для проверки принадлежности организации.
 */
@Configuration
public class CheckAccessConfiguration {

    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(EmployeeRepository employeeRepository,
                                                         DepartmentRepository departmentRepository) {
        return authenticated -> employeeRepository.findByUserId(authenticated)
                .map(Employee::getDepartmentId)
                .flatMap(departmentRepository::findById)
                .map(Department::getOrganizationId).orElse(null);
    }

}
