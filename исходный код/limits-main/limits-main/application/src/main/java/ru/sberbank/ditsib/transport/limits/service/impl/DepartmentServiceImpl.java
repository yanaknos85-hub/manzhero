package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.service.DepartmentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of department service.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Override
    @Cacheable(value = "department", key = "id")
    public Optional<Department> get(UUID id) {
        return departmentRepository.findById(id);
    }
    
    @Override
    @Cacheable(value = "department")
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }
    
    @Override
    @Cacheable(value = "department")
    public Optional<Department> get(UUID organizationId, UUID id) {
        return departmentRepository.findByIdAndOrganizationId(id, organizationId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "department", key = "department.id")
    public void delete(Department department) {
        department.setActive(false);
        departmentRepository.save(department);
    }
    
    @Override
    @Transactional
    @CachePut(value = "department", key = "department.id")
    public Department save(Department department) {
        return departmentRepository.save(department);
    }
    
    @Override
    public boolean exists(UUID organizationId, UUID departmentId) {
        return departmentRepository.existsByOrganizationIdAndId(organizationId, departmentId);
    }
    
    @Override
    @CachePut(value = "department", key = "code")
    public Department getByCode(String code) {
        List<Department> departmentList = departmentRepository.findByCode(code);
        if (departmentList.size() != 1) {
            return null;
        }
        return departmentList.getFirst();
    }
    
    @Override
    public List<Department> getByParent(UUID parentId) {
        return departmentRepository.findByParentId(parentId);
    }
    
    @Override
    public List<Department> getUpperLevelDepartment(UUID organizationId) {
        return departmentRepository.findByOrganizationIdAndParentIdIsNullAndActiveTrue(organizationId);
    }
}
