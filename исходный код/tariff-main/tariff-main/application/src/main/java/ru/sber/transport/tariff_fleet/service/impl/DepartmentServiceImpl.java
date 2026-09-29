package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.DepartmentRepository;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.service.DepartmentService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of department service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository repository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Department entity) {
        repository.save(entity.setActive(false));
    }
    
    @Override
    public void saveOrUpdate(Department entity) {
        var dbEntityOptional = repository.findById(entity.getId());
        if (dbEntityOptional.isPresent()) {
            repository.save(dbEntityOptional.get()
                                            .setActive(true)
                                            .setDepartmentName(entity.getDepartmentName())
                                            .setOrganizationId(entity.getOrganizationId())
                                            .setHumanReadableId(entity.getHumanReadableId())
                                            .setEasupId(entity.getEasupId())
                                            .setParentId(entity.getParentId()));
        } else {
            repository.save(entity);
        }
    }
}
