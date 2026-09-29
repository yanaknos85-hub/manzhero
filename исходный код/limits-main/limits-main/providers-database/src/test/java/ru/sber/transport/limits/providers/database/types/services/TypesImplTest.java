package ru.sber.transport.limits.providers.database.types.services;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.providers.Types;
import ru.sber.transport.limits.providers.database.types.TypesImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.limits.Tables.TYPES;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера типов услуг")
class TypesImplTest {

    @Autowired
    DSLContext context;

    private final Types types = new TypesImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Получение сервиса по коду")
    void test_get() {
        final var service = context.insertInto(TYPES)
                .set(TYPES.ID, Instancio.create(String.class))
                .returning()
                .fetchSingle();

        final var actualOpt = types.get(service.getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();
        assertThat(actual.name()).isEqualTo(service.getId());
    }

}