package ru.sberbank.ditsib.transport.sudir.providers.role.dao;

import org.jooq.JSON;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.sudir.tables.Role;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.providers.role.model.Scope;

import java.util.Collection;
import java.util.Set;

/**
 * repository for working with roles.
 */
public interface RoleRepository extends JooqRepository<Role, RoleRecord, String> {

    /**
     * Find roles of account.
     *
     * @param account account of roles.
     * @return roles.
     */
    Set<RoleRecord> findOfAccount(AccountRecord account);

    /**
     * Add role to account.
     *
     * @param savedAccount account.
     * @param roleCode role.
     */
    void addRole(AccountRecord savedAccount, RoleRecord roleCode);

    /**
     * Clear roles of account.
     *
     * @param savedAccount account.
     */
    void clearRoles(AccountRecord savedAccount);

    /**
     * Mapping of scopes.
     *
     * @param defaultFor source JSON.
     * @return collection of scopes.
     */
    Collection<Scope> mapScopes(JSON defaultFor);

    /**
     * Clear role mappings.
     *
     * @param roleCode role to clear.
     */
    void clearRole(String roleCode);
}
