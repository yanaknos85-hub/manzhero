package ru.sberbank.ditsib.transport.sudir.providers.role.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.sudir.tables.AccountRoles;
import ru.sber.transport.database.sudir.tables.Role;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.AccountRolesRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.model.Scope;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Repository
@Transactional
class RoleRepositoryImpl implements RoleRepository {

    private final ObjectMapper mapper;

    @Override
    public Set<RoleRecord> findOfAccount(AccountRecord account) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;
        return new HashSet<>(context().selectDistinct()
                .from(table())
                .innerJoin(accountRole).on(accountRole.ROLE_CODE.eq(table().CODE))
                .where(accountRole.ACCOUNT_ID.eq(account.getId()))
                .fetchInto(RoleRecord.class));
    }

    @Override
    public void addRole(AccountRecord savedAccount, RoleRecord roleCode) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;

        var accountRoleRecord = new AccountRolesRecord();
        accountRoleRecord.setAccountId(savedAccount.getId());
        accountRoleRecord.setRoleCode(roleCode.getCode());

        context().insertInto(accountRole).set(accountRoleRecord).onConflict().doNothing().execute();

    }

    @Override
    public void clearRoles(AccountRecord savedAccount) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;

        context().deleteFrom(accountRole).where(accountRole.ACCOUNT_ID.eq(savedAccount.getId())).execute();
    }

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public Collection<Scope> mapScopes(JSON defaultFor) {
        return mapper.readValue(defaultFor.data(), new TypeReference<>() {});
    }

    @Override
    public Role table() {
        return Role.ROLE;
    }

    @Override
    public void clearRole(String roleCode) {
        var accountRoles = AccountRoles.ACCOUNT_ROLES;
        context().deleteFrom(accountRoles).where(accountRoles.ROLE_CODE.eq(roleCode)).execute();
    }
}
