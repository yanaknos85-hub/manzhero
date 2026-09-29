package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;

import java.util.*;

/**
 * Implementation of employee service.
 */
@RequiredArgsConstructor
@Service
@Transactional
class EmployeeServiceImpl implements EmployeeService {

    private static final String USER_ID = "userId";
    private final EmployeeRepository repository;
    
    @Override
    public Employee save(Employee employee) {
        return repository.save(employee);
    }
    
    @Override
    public void delete(Employee employee) {
        repository.delete(employee);
    }
    
    @Override
    public Optional<Employee> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Employee getByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID, userId)));
    }

    @Override
    public Employee getEmployeeByIdWithApproveDepartments(UUID userId) {
        return repository.findByUserIdWithApproveDocuments(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID, userId)));
    }
    
    @Override
    public UUID getDepartmentIdByUserId(UUID userId) throws EntityNotFoundException {
        return repository.findDepartmentIdByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID, userId)));
    }

    @Override
    public List<UUID> getAllByDepartmentIds(Set<UUID> departmentIds) {
        return repository.findAllByDepartmentIdIs(departmentIds);
    }
}
