package ru.sberbank.ditsib.transport.sudir.providers.url;

import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.Allow;
import ru.sber.transport.database.sudir.tables.records.AllowRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.AllowUrlProvider;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка провайдера допустимых URL")
@ActiveProfiles("test")
class AllowUrlProviderImplTest {

    @Autowired
    private AllowUrlProvider provider;

    @Autowired
    private DSLContext context;

    @Test
    @DisplayName("Проверка разрешения")
    void test_allow() {
        context.insertInto(Allow.ALLOW).set(new AllowRecord("^.*/address$")).execute();

        assertThat(provider.allow("localhost:80/address")).isTrue();
        assertThat(provider.allow("localhost:80/address2")).isFalse();
        assertThat(provider.allow("localhost:80/2address")).isFalse();
    }

}