package ru.sberbank.ditsib.transport.sudir.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;
import ru.sberbank.ditsib.transport.sudir.business.model.Token;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.AccountNotFoundException;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.RefreshTokenExpired;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccessTokenProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccountProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.business.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.AccountSender;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;

import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация бизнес-случаев учетной записи.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class AccountsImpl implements Accounts {
    
    private final AccountProvider accountProvider;
    
    private final AccessTokenProvider accessTokenProvider;
    
    private final RefreshProvider refreshProvider;
    
    private final RoleProvider roleProvider;

    private final AccountSender accountSender;

    private final UserAgentSender userAgentSender;
    
    @Override
    public void logout(UUID userId, String token) {
        var account = accountProvider.get(userId, true);
        accessTokenProvider.toBlackList(token);
        account.ifPresent(acc -> refreshProvider.release(acc, token));
    }
    
    @Override
    public Token relogin(String refresh,String userAgent, String clientType) throws RefreshTokenExpired, InvalidKeySpecException {
        var account = refreshProvider.search(refresh).orElseThrow(() -> new RefreshTokenExpired(refresh));
        var oldAccessToken = refreshProvider.getAccessToken(refresh);
        accessTokenProvider.toBlackList(oldAccessToken);
        refreshProvider.release(refresh);
        var jwt = accessTokenProvider.generate(account);
        var newRefresh = refreshProvider.generate(jwt, account);
        accountSender.send(account, LocalDateTime.now(ZoneOffset.UTC));
        if(userAgent!=null && clientType!=null){
            userAgentSender.send(account, userAgent, clientType, null);
        }
        return Token.builder()
                    .accessToken(jwt)
                    .refreshToken(newRefresh)
                    .build();
    }

    @Override
    public void save(Account account) {
        if (account.getPersonnelNumber() == null) {
            log.warn("Account without personnel number received");
            return;
        }
        accountProvider.save(account);
        accountSender.send(account);
    }

    @Override
    public void activation(UUID id, String personnelNumber, Set<String> roles, boolean active) {
        if (id == null && personnelNumber == null) {
            log.warn("Cannot create temporary account without personnel number. ID from SCIM: %s".formatted(id));
            return;
        }
        var account = accountProvider.get(id, true).orElseGet(() -> {
            log.info("Creating temporary account. ID: %s, PN: %s".formatted(id, personnelNumber));
            return Account.builder().id(id).personnelNumber(personnelNumber).build();
        });
        account.setActive(active);
        account.getRoles().clear();
        var foundRoles = roleProvider.getRoles(roles);
        account.getRoles().putAll(foundRoles.stream().collect(Collectors.toMap(Role::getCode, r -> false)));
        accountProvider.save(account);
    }

    @Override
    public Token login(Account accountDto) throws AccountNotFoundException, InvalidKeySpecException {
        var personnelNumber = accountDto.getPersonnelNumber();
        var account = accountProvider.get(personnelNumber)
                                     .orElseThrow(() -> new AccountNotFoundException(personnelNumber));
        var accessToken = accessTokenProvider.generate(account);
        var refreshToken = refreshProvider.generate(accessToken, accountDto);
        accountSender.send(account, LocalDateTime.now(ZoneOffset.UTC));
        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    @Override
    public Set<Role> getRoles(UUID userId) throws AccountNotFoundException {
        var account = accountProvider.get(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        return roleProvider.getRoles(account);
    }
}
