package ru.sber.transport.corporate.providers.human_readable.department;

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
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.corporate.Tables.COMPANY_SQ;
import static ru.sber.transport.database.corporate.tables.Organization.ORGANIZATION;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера человекочитаемых идентификаторов подразделений")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, DepartmentHumanReadableProviderImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class DepartmentHumanReadableProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private HumanReadableProvider<Department> provider;

    @Test
    @DisplayName("Добавление нового")
    void test_new() {
        var organization = Instancio.create(Organization.class);
        var organizationRecord = new OrganizationRecord();
        organizationRecord.setId(organization.getId());
        organizationRecord.setDigitId(organization.getDigitId());
        organizationRecord.setOfficialName(organization.getName());
        organizationRecord.setAddress(organization.getAddress());
        organizationRecord.setMsrn(organization.getMsrn());
        organizationRecord.setTin(organization.getTid());

        context.insertInto(ORGANIZATION)
                .set(organizationRecord)
                .execute();

        var next = provider.getNext(organization.getId());

        assertThat(next).isEqualTo("DT-%04d-00000001".formatted(organization.getDigitId()));

        var companySq = context.selectFrom(COMPANY_SQ).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(companySq.getOrgdigitid()).isEqualTo(organization.getDigitId());
            it.assertThat(companySq.getSq()).isEqualTo(1);
            it.assertThat(companySq.getPrefix()).isEqualTo("DT");
        });
    }

    @Test
    @DisplayName("Обновление")
    void test_edit() {
        var organization = Instancio.create(Organization.class);
        var organizationRecord = new OrganizationRecord();
        organizationRecord.setId(organization.getId());
        organizationRecord.setDigitId(organization.getDigitId());
        organizationRecord.setOfficialName(organization.getName());
        organizationRecord.setAddress(organization.getAddress());
        organizationRecord.setMsrn(organization.getMsrn());
        organizationRecord.setTin(organization.getTid());

        context.insertInto(ORGANIZATION)
                .set(organizationRecord)
                .execute();

        context.insertInto(COMPANY_SQ)
                .set(COMPANY_SQ.ID, UUID.randomUUID())
                .set(COMPANY_SQ.PREFIX, "DT")
                .set(COMPANY_SQ.ORGDIGITID, organization.getDigitId())
                .set(COMPANY_SQ.SQ, BigInteger.valueOf(500))
                .set(COMPANY_SQ.DT_MODIFY, OffsetDateTime.now())
                .set(COMPANY_SQ.DT_INSERT, OffsetDateTime.now())
                .execute();

        var next = provider.getNext(organization.getId());

        assertThat(next).isEqualTo("DT-%04d-00000501".formatted(organization.getDigitId()));

        var companySq = context.selectFrom(COMPANY_SQ).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(companySq.getOrgdigitid()).isEqualTo(organization.getDigitId());
            it.assertThat(companySq.getSq()).isEqualTo(501);
            it.assertThat(companySq.getPrefix()).isEqualTo("DT");
        });
    }
}