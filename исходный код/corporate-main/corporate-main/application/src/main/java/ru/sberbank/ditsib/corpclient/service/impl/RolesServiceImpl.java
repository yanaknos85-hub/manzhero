package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.dao.RolesRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.service.RolesService;

import java.util.Optional;

/**
 * Реализация сервиса работы с ролями.
 */
@RequiredArgsConstructor
@Component
class RolesServiceImpl implements RolesService {
    
    private final RolesRepository repository;
    
    @Override
    public void delete(String code) {
        get(code).ifPresent(repository::delete);
    }
    
    @Override
    public Optional<Role> get(String code) {
        return repository.findById(code);
    }
    
    @Override
    public Optional<Role> getByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public void save(Role role) {
        repository.save(role);
    }
}
