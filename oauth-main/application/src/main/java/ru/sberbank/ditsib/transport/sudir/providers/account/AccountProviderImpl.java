package ru.sberbank.ditsib.transport.sudir.providers.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccessTokenProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccountProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.account.mapper.AccountMapper;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.model.Scope;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация провайдера УЗ.
 */
@RequiredArgsConstructor
@Transactional
@Component
class AccountProviderImpl implements AccountProvider {

    private final AccountRepository accountRepository;

    private final RoleRepository roleRepository;

    private final AccountMapper mapper;

    private final AccessTokenProvider accessTokenProvider;

    private final RefreshProvider refreshProvider;

    @Override
    public Optional<Account> get(String personnelNumber) {
        return accountRepository.findByActiveTrueAndPersonnelNumber(personnelNumber)
                .map(a -> new AbstractMap.SimpleEntry<>(a, roleRepository.findOfAccount(a)))
                .map(e -> mapper.toBusiness(e.getKey(), e.getValue()));
    }

    @Override
    public Optional<Account> get(UUID id, Boolean active) {
        var result = active == null ? accountRepository.findById(id) : accountRepository.findByActiveAndId(id, active);
        return result.map(a -> new AbstractMap.SimpleEntry<>(a, roleRepository.findOfAccount(a)))
                .map(e -> mapper.toBusiness(e.getKey(), e.getValue()));
    }

    @Override
    public void save(Account account) {
        var savedAccount = accountRepository.findById(account.getId()).orElseGet(AccountRecord::new);
        if (!checkRoles(savedAccount, account)) {
            var tokens = refreshProvider.searchTokenData(account).keySet();
            tokens.forEach(accessTokenProvider::toBlackList);
        }
        mapper.toModel(savedAccount, account);

        accountRepository.save(savedAccount);

        var savedRoles = roleRepository.findOfAccount(savedAccount);
        if (savedRoles.isEmpty() && account.getRoles() == null) {
            var scope = Optional.ofNullable(account.getScope()).map(Scope::valueOf).orElse(Scope.EMPLOYEE);

            var roleSet = roleRepository.findAll().stream()
                    .filter(r -> roleRepository.mapScopes(r.getDefaultFor()).contains(scope)).collect(Collectors.toUnmodifiableSet());
            for (var roleCode : roleSet) {
                roleRepository.addRole(savedAccount, roleCode);
            }
        }

        if (account.getRoles() != null) {
            roleRepository.clearRoles(savedAccount);
            for (var roleCode : account.getRoles().keySet()) {
                roleRepository.findById(roleCode).ifPresent(role -> roleRepository.addRole(savedAccount, role));
            }
        }
    }

    @SuppressWarnings("java:S3958")
    private boolean checkRoles(AccountRecord databaseAccount, Account sourceAccount) {
        var roles = sourceAccount.getRoles();
        if (roles != null) {
            var dbRoles = roleRepository.findOfAccount(databaseAccount).stream().sorted(Comparator.comparing(RoleRecord::getCode)).map(RoleRecord::getCode).toList();
            var sourceRoles = roles.keySet().stream().sorted(Comparator.comparing(Function.identity())).toList();
            return !dbRoles.stream().allMatch(role -> sourceRoles.stream().allMatch(role::equals));
        }
        return true;
    }
}
