package ru.sber.transport.limits.providers.database.services;

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
import ru.sber.transport.database.limits.tables.records.ServicesRecord;
import ru.sber.transport.limits.model.Service;
import ru.sber.transport.limits.providers.Services;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.limits.Tables.SERVICES;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера видов услуг")
class ServicesImplTest {

    @Autowired
    DSLContext context;

    private final Services services = new ServicesImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Получение сервиса по коду")
    void test_get() {
        final var service = context.insertInto(SERVICES)
                .set(SERVICES.ID, Instancio.create(String.class))
                .returning()
                .fetchSingle();

        final var actualOpt = services.get(service.getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();
        assertThat(actual.name()).isEqualTo(service.getId());
    }

    @Test
    @DisplayName("Получение сервисов")
    void test_getAll() {
        final var servicesList = IntStream.range(0, 10).mapToObj(i -> context.insertInto(SERVICES)
                .set(SERVICES.ID, Instancio.create(String.class))
                .returning()
                .fetchSingle())
                .toList();

        final var actual = services.get();

        assertThat(actual.stream().map(Service::name).toList()).containsAll(servicesList.stream().map(ServicesRecord::getId).toList());
    }

}