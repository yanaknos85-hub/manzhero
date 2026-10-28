package ru.sberbank.ditsib.transport.sudir.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.RefreshTokenExpired;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccessTokenProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccountProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.AccountSender;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;

import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка бизнес-случаев работы с УЗ")
class AccountsImplTest {

    @Captor
    private ArgumentCaptor<String> oldJwtCaptor;

    @Captor
    private ArgumentCaptor<String> oldRefreshCaptor;

    @Captor
    private ArgumentCaptor<String> jwtCaptor;

    @Captor
    private ArgumentCaptor<String> refreshCaptor;

    @Captor
    private ArgumentCaptor<Account> accountListCaptor;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Captor
    private ArgumentCaptor<List<UUID>> accountIdsCaptor;

    @Captor
    private ArgumentCaptor<String> blTokenCaptor;

    @Captor
    private ArgumentCaptor<String> relTokenCaptor;

    private final AccountProvider accountProvider = mock(AccountProvider.class);

    private final AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);

    private final RefreshProvider refreshProvider = mock(RefreshProvider.class);

    private final RoleProvider roleProvider = mock(RoleProvider.class);

    private final AccountSender accountSender = mock(AccountSender.class);

    private final UserAgentSender userAgentSender = mock(UserAgentSender.class);

    private final Accounts cases = new AccountsImpl(accountProvider,
            accessTokenProvider,
            refreshProvider, roleProvider, accountSender, userAgentSender);

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Проверка выхода")
    @Test
    void test_logout() {
        var userId = UUID.randomUUID();
        var userLogin = "Login";
        var token = "JWT";

        var account = Account.builder().personnelNumber(userLogin)
                .build();

        when(accountProvider.get(eq(userId), anyBoolean())).thenReturn(Optional.of(account));

        cases.logout(userId, token);

        verify(accessTokenProvider).toBlackList(blTokenCaptor.capture());
        verify(refreshProvider).release(accountCaptor.capture(), relTokenCaptor.capture());

        var blToken = blTokenCaptor.getValue();
        var relToken = relTokenCaptor.getValue();
        var actual = accountCaptor.getValue();

        assertThat(blToken)
                .isEqualTo(relToken)
                .isEqualTo(token);
        assertThat(relToken).isEqualTo(token);
        assertThat(actual).isEqualTo(account);
    }

    @DisplayName("Проверка обновления сессии")
    @Test
    void test_relogin() throws RefreshTokenExpired, InvalidKeySpecException {
        var account = Account.builder().personnelNumber("Login").id(UUID.randomUUID()).build();
        var refreshToken = "Refresh";
        var oldAccessToken = "JWT";
        var newAccessToken = "New JWT";
        var newRefreshToken = "Refreshed";

        when(refreshProvider.search(refreshToken)).thenReturn(Optional.of(account));
        when(refreshProvider.getAccessToken(refreshToken)).thenReturn(oldAccessToken);
        when(accessTokenProvider.generate(account)).thenReturn(newAccessToken);
        when(refreshProvider.generate(newAccessToken, account)).thenReturn(newRefreshToken);

        var actual = cases.relogin(refreshToken, null, null);

        var idsCaptor = ArgumentCaptor.forClass(Account.class);
        var issuedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(accessTokenProvider).toBlackList(oldJwtCaptor.capture());
        verify(accountSender).send(idsCaptor.capture(), issuedAtCaptor.capture());
        verify(refreshProvider).release(oldRefreshCaptor.capture());

        assertThat(idsCaptor.getValue()).isEqualTo(account);
        assertThat(issuedAtCaptor.getValue()).isNotNull();
        assertThat(issuedAtCaptor.getValue().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(oldJwtCaptor.getValue()).isEqualTo(oldAccessToken);
        assertThat(oldRefreshCaptor.getValue()).isEqualTo(refreshToken);
        assertThat(actual.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(actual.getRefreshToken()).isEqualTo(newRefreshToken);
    }

    @DisplayName("Проверка обновления сессии. Токен истек")
    @Test
    void test_relogin_expired() {
        assertThatThrownBy(() -> cases.relogin("Any token", null, null))
                .isInstanceOf(RefreshTokenExpired.class)
                .hasMessage(String.format("Refresh token '%s' was expired", "Any token"));
    }

    @DisplayName("Проверка сохранения")
    @Test
    void test_save() {
        var account = Account.builder().id(UUID.randomUUID()).personnelNumber("Login").build();

        cases.save(account);

        var idsCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountProvider).save(accountListCaptor.capture());
        verify(accountSender).send(idsCaptor.capture());

        var actual = accountListCaptor.getValue();
        var actualIds = idsCaptor.getValue();

        assertThat(actual.getPersonnelNumber()).isEqualTo(account.getPersonnelNumber());
        assertThat(actualIds.getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("Привязка ролей")
    void test_linkRoles() {
        var personnelNumber = Instancio.create(String.class);
        var roles = Instancio.ofSet(String.class)
                .create();
        var account = Instancio.of(Account.class)
                .set(Select.field(Account::getPersonnelNumber), personnelNumber)
                .set(Select.field(Account::isActive), true)
                .set(Select.field(Account::getRoles), new HashMap<>())
                .create();
        var foundRoles = Instancio.ofSet(Role.class)
                .generate(Select.field(Role::getCode), gen -> gen.oneOf(roles))
                .create();

        when(accountProvider.get(eq(account.getId()), any())).thenReturn(Optional.of(account));
        when(roleProvider.getRoles(roles)).thenReturn(foundRoles);

        cases.activation(account.getId(), personnelNumber, roles, account.isActive());

        var accountsCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountProvider).save(accountsCaptor.capture());

        var actualRaw = accountsCaptor.getValue();
        assertThat(actualRaw).isInstanceOf(Account.class);

        assertThat(actualRaw.getId()).isEqualTo(account.getId());
        assertThat(actualRaw.getPersonnelNumber()).isEqualTo(account.getPersonnelNumber());
        assertThat(actualRaw.getScope()).isEqualTo(account.getScope());
        assertThat(actualRaw.getRoles().keySet()).hasSameElementsAs(foundRoles.stream().map(Role::getCode).collect(Collectors.toSet()));
        assertThat(actualRaw.isActive()).isEqualTo(account.isActive());
    }

    @Test
    @DisplayName("Привязка ролей. Нет УЗ")
    void test_linkRoles_no_account() {
        var personnelNumber = Instancio.create(String.class);
        var id = UUID.randomUUID();
        var roles = Instancio.ofSet(String.class)
                .create();
        var activation = Instancio.create(Boolean.class);
        var foundRoles = Instancio.ofSet(Role.class)
                .generate(Select.field(Role::getCode), gen -> gen.oneOf(roles))
                .create();

        when(accountProvider.get(id)).thenReturn(Optional.empty());
        when(roleProvider.getRoles(roles)).thenReturn(foundRoles);

        cases.activation(id, personnelNumber, roles, activation);

        var accountsCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountProvider).save(accountsCaptor.capture());

        var actualList = accountsCaptor.getValue();

        assertThat(actualList).isInstanceOf(Account.class);

        assertThat(actualList.getId()).isNotNull();
        assertThat(actualList.getPersonnelNumber()).isEqualTo(personnelNumber);
        assertThat(actualList.getScope()).isNull();
        assertThat(actualList.getRoles().keySet()).hasSameElementsAs(foundRoles.stream().map(Role::getCode).collect(Collectors.toSet()));
        assertThat(actualList.isActive()).isEqualTo(activation);
    }
}