package ru.sberbank.ditsib.transport.sudir.providers.account;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccessTokenProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccountProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.account.mapper.AccountMapper;
import ru.sberbank.ditsib.transport.sudir.providers.account.mapper.AccountMapperImpl;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка провайдера учетной записи")
class AccountProviderImplTest {

    private final AccountRepository accounts = Mockito.mock(AccountRepository.class);

    private final RoleRepository roles = Mockito.mock(RoleRepository.class);

    private final AccountMapper mapper = new AccountMapperImpl();

    private final AccessTokenProvider accesses = Mockito.mock(AccessTokenProvider.class);

    private final RefreshProvider refreshes = Mockito.mock(RefreshProvider.class);

    private final AccountProvider accountProvider = new AccountProviderImpl(accounts, roles, mapper, accesses, refreshes);

    @Test
    @DisplayName("Получить учетную запись по коду")
    void test_getAccount() {
        var personnelNumber = "12345";
        var account = new AccountRecord();
        account.setActive(true);

        var roleRecord = new RoleRecord();
        roleRecord.setCode("ROLE_TEST");
        roleRecord.setDataMaster(false);

        var rolesData = new HashSet<RoleRecord>();
        rolesData.add(roleRecord);

        when(accounts.findByActiveTrueAndPersonnelNumber(personnelNumber)).thenReturn(Optional.of(account));
        when(roles.findOfAccount(any())).thenReturn(rolesData);

        var accountOpt = accountProvider.get(personnelNumber);

        assertThat(accountOpt.isPresent()).isTrue();
    }

}