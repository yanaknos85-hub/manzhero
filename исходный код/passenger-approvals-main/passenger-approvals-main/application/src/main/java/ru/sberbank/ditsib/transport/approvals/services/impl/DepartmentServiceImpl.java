package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.transport.approvals.services.DepartmentService;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Departments;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of department service.
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository repository;
    private final Departments departments;
    
    @Override
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Department entity) {
        repository.save(entity.setActive(false));
    }
    
    @Override
    public Department save(Department entity) {
        return repository.save(entity);
    }

    @Override
    public Set<Department> findByDepartmentHeadId(UUID headId) {
        return repository.findByDepartmentHeadId(headId);
    }

    @SneakyThrows
    @Override
    public void saveGrpcEntity(String message, UUID id) {
        this.save(Optional.ofNullable(departments.one(id)).orElseThrow(() -> {
            log.info(message);
            throw new AwaitingSynchronizationException("Awaiting an department synchronization");
        }));
    }
}
