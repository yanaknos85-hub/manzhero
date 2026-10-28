package ru.sberbank.ditsib.transport.sudir.providers.role;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.messaging.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Тест провайдера ролей")
@ActiveProfiles("test")
@Transactional
public class RoleProviderImplTest {

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private ru.sberbank.ditsib.transport.sudir.business.providers.RoleProvider businessRoleProvider;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Тест удаления роли")
    void test_deleteRole() {
        var roleCode = "ROLE_TEST";
        var accountId = UUID.randomUUID();

        var role = new RoleRecord();
        role.setCode(roleCode);
        role.setName("name");
        role.setDescription("description");
        role.setDataMaster(false);

        var account = new AccountRecord();
        account.setId(accountId);
        account.setActive(true);
        account.setPersonnelNumber("12345");

        roleRepository.save(role);
        accountRepository.save(account);
        roleRepository.addRole(account, role);

        var rolesInBetween = roleRepository.findAll();
        var accountRolesInBetween = roleRepository.findOfAccount(account);

        assertThat(rolesInBetween.size()).isEqualTo(1);
        assertThat(accountRolesInBetween.size()).isEqualTo(1);

        roleProvider.delete(roleCode);

        var roleOpt = roleRepository.findById(roleCode);
        var accountRoleList = roleRepository.findOfAccount(account);

        assertThat(roleOpt).isEmpty();
        assertThat(accountRoleList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Тест получения списка ролей. Нет ролей")
    void test_getRoles_no_roles() {
        var account = Instancio.create(Account.class);

        assertThat(businessRoleProvider.getRoles(account)).isEmpty();
    }

    @Test
    @DisplayName("Тест получения списка ролей пользователя")
    void test_user_getRoles() {
        var account = Instancio.create(Account.class);

        var accountRecord = new AccountRecord();
        accountRecord.setPersonnelNumber(account.getPersonnelNumber());
        accountRecord.setId(UUID.randomUUID());

        var roleRecord = new RoleRecord();
        roleRecord.setCode("ROLE_CODE");
        roleRecord.setName("ROLE_NAME");

        accountRepository.save(accountRecord);
        roleRepository.save(roleRecord);

        roleRepository.addRole(accountRecord, roleRecord);

        var roles = businessRoleProvider.getRoles(account);
        assertThat(roles).isNotEmpty().hasSize(1);
        assertThat(roles.iterator().next().getCode()).isEqualTo(roleRecord.getCode());
    }

    @Test
    @DisplayName("Тест получения списка ролей")
    void test_getRoles() {
        var roleRecord = new RoleRecord();
        roleRecord.setCode("ROLE_CODE");
        roleRecord.setName("ROLE_NAME");

        roleRepository.save(roleRecord);

        var roles = businessRoleProvider.getRoles(Set.of(roleRecord.getCode()));
        assertThat(roles).isNotEmpty().hasSize(1);
        assertThat(roles.iterator().next().getCode()).isEqualTo(roleRecord.getCode());
    }
}
