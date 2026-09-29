package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Executor group repository
 */
@Repository
public interface ExecutorGroupRepository extends JpaSpecificationExecutor<ExecutorGroup>, JpaRepository<ExecutorGroup, UUID> {

    Boolean existsExecutorGroupByNameEqualsAndServiceEqualsAndActiveTrue(String executorGroupName, String service);

    Boolean existsExecutorGroupByServiceLevelEqualsAndServiceEqualsAndActiveTrue(String executorGroupName, String service);

    List<ExecutorGroup> findByOrganizationsInAndActiveTrue(Set<Organization> organizations);

    List<ExecutorGroup> findByOrganizationsInAndServiceAndActiveTrue(Set<Organization> organizations, String service);

    List<ExecutorGroup> findByOrganizationsInAndDepartmentsInAndActiveTrue(Set<Organization> organizations, Set<Department> departments);

    Stream<ExecutorGroup> findByNameAndActiveTrue(String name);
}
