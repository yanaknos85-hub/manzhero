package ru.sberbank.ditsib.transport.sudir.providers.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.sudir.tables.records.SessionRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.config.RtProperties;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.dao.SessionRepository;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.mapper.SessionMapper;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация провайдера токена обновления сессии.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
@EnableScheduling
class RefreshProviderImpl implements RefreshProvider {

    private final SessionRepository sessionRepository;

    private final AccountRepository accountRepository;

    private final RoleRepository roleRepository;

    private final RtProperties rtProperties;

    private final SessionMapper sessionMapper;

    private final Clock clock;

    @Scheduled(cron = "0 */1 * * * *")
    void cleanupOldSessions() {
        var now = LocalDateTime.now(clock);
        var oldSessions = sessionRepository.findAllByExpiredAtBefore(now);
        oldSessions.stream().map(SessionRecord::getId).map(String::valueOf).forEach(this::release);
        if (!oldSessions.isEmpty()) {
            log.info("%s expired sessions released".formatted(oldSessions.size()));
        }
    }

    @Override
    public String generate(String accessToken, Account account) {
        var expireData = rtProperties.getExpire();
        var expirationDate = LocalDateTime.now(ZoneOffset.UTC)
                .plusSeconds(expireData.getSeconds())
                .plusMinutes(expireData.getMinutes())
                .plusHours(expireData.getHours())
                .plusDays(expireData.getDays())
                .plusMonths(expireData.getMonths())
                .plusYears(expireData.getYears());

        var session = new SessionRecord();

        session.setId(UUID.randomUUID());
        session.setIssuedAt(LocalDateTime.now(ZoneOffset.UTC));
        session.setToken(accessToken);
        session.setExpireAt(expirationDate);
        session.setAccountId(accountRepository.getByPersonnelNumber(account.getPersonnelNumber()).getId());

        session = sessionRepository.save(session);
        return session.getId().toString();
    }

    @Override
    public void release(Account account, String token) {
        var session = sessionRepository.findByToken(token);
        session.ifPresent(sessionRepository::delete);
    }

    @Override
    public Optional<Account> search(String refresh) {
        return sessionRepository.findByIdAndExpiredAtAfter(UUID.fromString(refresh), LocalDateTime.now(ZoneOffset.UTC))
                .map(SessionRecord::getAccountId)
                .flatMap(accountRepository::findById)
                .map(a -> new AbstractMap.SimpleEntry<>(a, roleRepository.findOfAccount(a)))
                .map(e -> sessionMapper.toBusiness(e.getKey(), e.getValue()));
    }

    @Override
    public synchronized void release(String refresh) {
        sessionRepository.findById(UUID.fromString(refresh)).ifPresent(sessionRepository::delete);
    }

    @Override
    public Map<String, String> searchTokenData(Account account) {
        return sessionRepository
                .findAllByAccountAndExpiredAtAfter(account.getPersonnelNumber(), LocalDateTime.now(ZoneOffset.UTC))
                .stream().collect(Collectors.toMap(SessionRecord::getToken, session -> session.getId().toString()));
    }

    @Override
    public String getAccessToken(String refresh) {
        return sessionRepository.getById(UUID.fromString(refresh)).getToken();
    }
}
