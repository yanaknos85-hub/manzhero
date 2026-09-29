package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.EmployeeRepository;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.exception.UserNotFoundException;
import ru.sber.transport.tariff_fleet.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of employee service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    @Override
    public Optional<Employee> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public Employee getByUserId(UUID userId) {
        return repository.findWithOrganizationByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    @Override
    public void delete(Employee entity) {
        repository.save(entity
                .setActive(false));
    }
    
    @Override
    public void saveOrUpdate(Employee entity) {
        var dbEntityOptional = repository.findById(entity.getId());
        if (dbEntityOptional.isPresent()) {
            repository.save(dbEntityOptional.get()
                                            .setActive(true)
                                            .setDepartment(entity.getDepartment())
                                            .setPosition(entity.getPosition())
                                            .setOrganization(entity.getOrganization())
                                            .setFirstName(entity.getFirstName())
                                            .setLastName(entity.getLastName())
                                            .setPatronymic(entity.getPatronymic())
                                            .setHumanReadableId(entity.getHumanReadableId())
                                            .setPersonnelNumber(entity.getPersonnelNumber())
                                            .setMobilePhone(entity.getMobilePhone())
                                            .setUserId(entity.getUserId())
                                            .setCostCenter(entity.getCostCenter()));
        } else {
            repository.save(entity);
        }
    }

}
