package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee_;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of employee service.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository repository;

    private final EntityManager entityManager;
    
    @Override
    @Transactional
    public Employee save(Employee employee) {
        return repository.save(employee);
    }
    
    @Override
    @Transactional
    public void delete(Employee employee) {
        employee.setActive(false);
        repository.save(employee);
    }
    
    @Override
    public Optional<Employee> get(UUID employeeId) {
        return employeeId == null ? Optional.empty() : repository.findById(employeeId);
    }
    
    @Override
    public List<Employee> getAllById(List<UUID> listEmployeeId) {
    return repository.findAllById(listEmployeeId);
    }
    
    @Override
    public Optional<Employee> getByUserId(UUID id) {
        return repository.findByUserId(id);
    }
    
    @Override
    public Employee getByPersonnelNumber(String personnelNumber) {
        List<Employee> employeeList = repository.findByPersonnelNumber(personnelNumber);
        if (employeeList.size() != 1) {
            return null;
        }
        return employeeList.getFirst();
    }
    
    @Override
    public List<Employee> findByDepartmentIdAndActive(UUID departmentId, Boolean isActive) {
        return repository.findByDepartmentIdAndActive(departmentId, isActive);
    }
    
    @Override
    public int countByDepartmentIdAndActive(UUID departmentId, Boolean isActive) {
        return repository.countByDepartmentIdAndActive(departmentId, isActive);
    }
    
    @Override
    public int countByOrganizationIdAndActive(Collection<UUID> organizationId, Boolean isActive) {
        return repository.countByOrganizationIdInAndActive(organizationId, isActive);
    }

    @Override
    public Map<UUID, Long> countByDepartmentsAndActive(Set<UUID> departments, boolean active) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(AbstractMap.SimpleEntry.class);
        var root = query.from(Employee.class);
        var predicate = cb.and(root.get(Employee_.departmentId).in(departments), cb.equal(root.get(Employee_.active), active));
        var multiselect = query.multiselect(root.get(Employee_.departmentId).alias("departmentId"), cb.count(root.get(Employee_.departmentId)).alias("count"));
        var emQuery = entityManager.createQuery(multiselect.where(predicate).groupBy(root.get(Employee_.departmentId)));
        return emQuery.getResultStream().collect(Collectors.toMap(it -> UUID.fromString(String.valueOf(it.getKey())), it -> Long.valueOf(String.valueOf(it.getValue()))));
    }
}
