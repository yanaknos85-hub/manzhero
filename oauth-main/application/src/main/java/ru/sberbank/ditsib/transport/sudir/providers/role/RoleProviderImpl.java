package ru.sberbank.ditsib.transport.sudir.providers.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;
import ru.sberbank.ditsib.transport.sudir.business.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.mapper.RolesMapper;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация провайдера ролей.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
class RoleProviderImpl implements ru.sberbank.ditsib.transport.sudir.messaging.providers.RoleProvider, RoleProvider {
    
    private final AccountRepository accountRepository;
    
    private final RoleRepository roleRepository;
    
    private final RolesMapper rolesMapper;
    
    @Override
    public void delete(String code) {
        roleRepository.findById(code).ifPresent(
                role -> {
                    roleRepository.clearRole(role.getCode());
                    roleRepository.delete(role);
                });
    }
    
    @Override
    public void save(RoleMessage message) {
        var role = rolesMapper.toModel(message);
        roleRepository.save(role);
    }
    
    @Override
    public Set<Role> getRoles(Account account) {
        var db = accountRepository.findByActiveTrueAndPersonnelNumber(account.getPersonnelNumber()).orElse(null);
        if (db != null) {
            var roles = roleRepository.findOfAccount(db);
            return roles.stream().map(rolesMapper::toBusiness).collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<Role> getRoles(Set<String> roles) {
        return roleRepository.findAllById(roles).stream().map(rolesMapper::toBusiness).collect(Collectors.toSet());
    }
}
