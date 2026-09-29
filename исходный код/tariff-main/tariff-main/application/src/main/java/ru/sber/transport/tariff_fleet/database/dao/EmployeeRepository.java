package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Employee_;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository of employees
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    
    /**
     * Find employee by ID with organization info.
     *
     * @param userId ID of user.
     *
     * @return Optional of {@link Employee}.
     */
    @EntityGraph(attributePaths = { Employee_.ORGANIZATION })
    Optional<Employee> findWithOrganizationByUserId(UUID userId);
}
