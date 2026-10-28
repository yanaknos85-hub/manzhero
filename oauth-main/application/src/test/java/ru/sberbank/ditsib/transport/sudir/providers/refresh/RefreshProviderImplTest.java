package ru.sberbank.ditsib.transport.sudir.providers.refresh;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.SessionRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.business.providers.RefreshProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.dao.SessionRepository;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка провайдера рефреш-токенов")
@ActiveProfiles("test")
class RefreshProviderImplTest {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RefreshProvider refreshProvider;

    @Autowired
    private JdbcTemplate template;
    
    @MockitoBean
    private Clock clock;

    @Test
    @DisplayName("Поиск по токену")
    void test_findByToken() {
        var token = Instancio.create(String.class);
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setActive(true);
        account.setPersonnelNumber("pn");

        accountRepository.save(account);

        template.update("insert into \"sudir\".\"session\" (id, token, expire_at, account_id, issued_at) values(?, ?, ?, ?, ?)", UUID.randomUUID(), token, LocalDateTime.now(), account.getId(), LocalDateTime.now());

        var actualOpt = sessionRepository.findByToken(token);
        assertThat(actualOpt).isPresent();
        assertThat(actualOpt.get().getToken()).isEqualTo(token);
        assertThat(actualOpt.get().getAccountId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("Поиск по времени истечения")
    void test_findByExpiration() {
        var token = Instancio.create(String.class);
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setActive(true);
        account.setPersonnelNumber("pn");

        accountRepository.save(account);

        template.update("insert into \"sudir\".\"session\" (id, token, expire_at, account_id, issued_at) values(?, ?, ?, ?, ?)", UUID.randomUUID(), token, LocalDateTime.now(), account.getId(), LocalDateTime.now());

        var actualOpt = sessionRepository.findAllByAccountAndExpiredAtAfter(account.getPersonnelNumber(), LocalDateTime.now().minusDays(1));
        assertThat(actualOpt).hasSize(1);
        assertThat(actualOpt.getFirst().getToken()).isEqualTo(token);
        assertThat(actualOpt.getFirst().getAccountId()).isEqualTo(account.getId());
    }
    
    @DisplayName("Очистка старых сессий")
    @Test
    void test_cleanupOldSessions() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setActive(true);
        account.setPersonnelNumber("0");
    
        account = accountRepository.save(account);
        var now = LocalDateTime.now();
    
        var count = 100;
        for (var i = 0; i < count; i++) {
            var session = new SessionRecord();
            session.setId(UUID.randomUUID());
            session.setAccountId(account.getId());
            session.setToken("blablabla"  + i);
            session.setExpireAt(now.minusDays(count / 2).plusDays(i));
            
            sessionRepository.save(session);
        }
        
        assertThat(sessionRepository.count()).isEqualTo(count);
    
        ((RefreshProviderImpl) refreshProvider).cleanupOldSessions();
    
        assertThat(sessionRepository.count()).isEqualTo(count / 2);
    
        for (var session : sessionRepository.findAll()) {
            assertThat(session.getExpireAt().truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        }
    }

}