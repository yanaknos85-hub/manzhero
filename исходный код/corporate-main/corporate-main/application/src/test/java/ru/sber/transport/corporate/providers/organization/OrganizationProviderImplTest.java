package ru.sber.transport.corporate.providers.organization;

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
import ru.sber.transport.corporate.business.providers.OrganizationProvider;
import ru.sber.transport.corporate.providers.organization.mappers.OrganizationDatabaseMapperImpl;
import ru.sber.transport.database.corporate.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.corporate.Tables.ORGANIZATION;
import static ru.sber.transport.database.corporate.Tables.ORGANIZATION_GROUP;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера организаций")
@JooqTest(properties = "logging.level.org.jooq.tools.LoggerListener=DEBUG")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, OrganizationProviderImpl.class, OrganizationDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class OrganizationProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private OrganizationProvider provider;

    @Test
    @DisplayName("Получение организации для синхронизации")
    void test_getSync() {
        var organizationId = Instancio.create(String.class);

        var saved = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .set(ORGANIZATION.SYNC_ID, organizationId)
                .returning().fetchSingle();

        var actualOpt = provider.get(organizationId);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.get();

        assertThat(actual.getId()).isEqualTo(saved.getId());
        assertThat(actual.getSyncId()).isEqualTo(saved.getSyncId());
    }

    @Test
    @DisplayName("Получение организации")
    void test_get() {
        var organizationId = Instancio.create(String.class);

        var organizationGroup = context.insertInto(ORGANIZATION_GROUP)
                .set(ORGANIZATION_GROUP.ID, UUID.randomUUID())
                .set(ORGANIZATION_GROUP.NAME, Instancio.create(String.class))
                .set(ORGANIZATION_GROUP.INTERNAL, Instancio.create(Boolean.class))
                .returning().fetchSingle();

        var saved = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .set(ORGANIZATION.SYNC_ID, organizationId)
                .set(ORGANIZATION.DIGIT_ID, Instancio.create(Long.class))
                .set(ORGANIZATION.ORGANIZATION_CODE, Instancio.create(Integer.class))
                .set(ORGANIZATION.ORGANIZATION_GROUP_ID, organizationGroup.getId())
                .returning().fetchSingle();

        final var org = context.insertInto(Tables.TRANSPORT_ORG)
                .set(Tables.TRANSPORT_ORG.ID, UUID.randomUUID())
                .set(Tables.TRANSPORT_ORG.ORGANIZATION_ID, saved.getId())
                .set(Tables.TRANSPORT_ORG.TRANSPORT_TYPE, Instancio.create(String.class))
                .returning().fetchSingle();

        final var actualOpt = provider.get(saved.getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(saved.getId());
            it.assertThat(actual.getSyncId()).isEqualTo(saved.getSyncId());
            it.assertThat(actual.getAvailableClasses()).isNotEmpty().hasSameElementsAs(List.of(org.getTransportType()));
            it.assertThat(actual.getContacts()).isEmpty();
            it.assertThat(actual.getStatus().name()).isEqualTo(saved.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(saved.getOfficialName());
            it.assertThat(actual.getAddress()).isEqualTo(saved.getAddress());
            it.assertThat(actual.getCode()).isEqualTo(saved.getOrganizationCode());
            it.assertThat(actual.getTid()).isEqualTo(saved.getTin());
            it.assertThat(actual.getMsrn()).isEqualTo(saved.getMsrn());
            it.assertThat(actual.getDigitId()).isEqualTo(saved.getDigitId());
            it.assertThat(actual.getGroupId()).isEqualTo(saved.getOrganizationGroupId());
        });
    }

    @Test
    @DisplayName("Получение организаций")
    void test_get_all() {
        var organizationId = Instancio.create(String.class);

        var saved = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .set(ORGANIZATION.SYNC_ID, organizationId)
                .returning().fetchSingle();

        var saved2 = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .returning().fetchSingle();

        final var org = context.insertInto(Tables.TRANSPORT_ORG)
                .set(Tables.TRANSPORT_ORG.ID, UUID.randomUUID())
                .set(Tables.TRANSPORT_ORG.ORGANIZATION_ID, saved.getId())
                .set(Tables.TRANSPORT_ORG.TRANSPORT_TYPE, Instancio.create(String.class))
                .returning().fetchSingle();

        final var org2 = context.insertInto(Tables.TRANSPORT_ORG)
                .set(Tables.TRANSPORT_ORG.ID, UUID.randomUUID())
                .set(Tables.TRANSPORT_ORG.ORGANIZATION_ID, saved2.getId())
                .set(Tables.TRANSPORT_ORG.TRANSPORT_TYPE, Instancio.create(String.class))
                .returning().fetchSingle();

        final var actualList = provider.get();

        assertThat(actualList).hasSize(2);

        assertSoftly(it -> {
            final var actual = actualList.getFirst();
            it.assertThat(actual.getId()).isEqualTo(saved.getId());
            it.assertThat(actual.getSyncId()).isEqualTo(saved.getSyncId());
            it.assertThat(actual.getAvailableClasses()).isNotEmpty().hasSameElementsAs(List.of(org.getTransportType()));
            it.assertThat(actual.getContacts()).isEmpty();
            it.assertThat(actual.getStatus().name()).isEqualTo(saved.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(saved.getOfficialName());
            it.assertThat(actual.getAddress()).isEqualTo(saved.getAddress());
            it.assertThat(actual.getCode()).isEqualTo(saved.getOrganizationCode());
            it.assertThat(actual.getTid()).isEqualTo(saved.getTin());
            it.assertThat(actual.getMsrn()).isEqualTo(saved.getMsrn());
        });

        assertSoftly(it -> {
            final var actual = actualList.get(1);
            it.assertThat(actual.getId()).isEqualTo(saved2.getId());
            it.assertThat(actual.getSyncId()).isEqualTo(saved2.getSyncId());
            it.assertThat(actual.getAvailableClasses()).isNotEmpty().hasSameElementsAs(List.of(org2.getTransportType()));
            it.assertThat(actual.getContacts()).isEmpty();
            it.assertThat(actual.getStatus().name()).isEqualTo(saved2.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(saved2.getOfficialName());
            it.assertThat(actual.getAddress()).isEqualTo(saved2.getAddress());
            it.assertThat(actual.getCode()).isEqualTo(saved2.getOrganizationCode());
            it.assertThat(actual.getTid()).isEqualTo(saved2.getTin());
            it.assertThat(actual.getMsrn()).isEqualTo(saved2.getMsrn());
        });
    }

}