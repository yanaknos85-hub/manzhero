package ru.sber.transport.corporate.providers.position.active_classes;

import io.qameta.allure.Feature;
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
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.database.corporate.enums.StructureType;
import ru.sber.transport.database.corporate.tables.Organization;
import ru.sber.transport.database.corporate.tables.Position;
import ru.sber.transport.database.corporate.tables.PositionTaxiClasses;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера согласований")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, AvailableClassesProviderImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class AvailableClassesProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private AvailableClassesProvider provider;

    @Test
    @DisplayName("Получение списка допустимых типов транспорта")
    void test_get() {
        var organizationId = UUID.randomUUID();

        context.insertInto(Organization.ORGANIZATION)
                .set(Organization.ORGANIZATION.ID, organizationId)
                .set(Organization.ORGANIZATION.OFFICIAL_NAME, "name")
                .set(Organization.ORGANIZATION.TIN, "tin")
                .set(Organization.ORGANIZATION.MSRN, "msrn")
                .set(Organization.ORGANIZATION.ADDRESS, "address")
                .execute();

        var positionId = UUID.randomUUID();
        context.insertInto(Position.POSITION)
                .set(Position.POSITION.ID, positionId)
                .set(Position.POSITION.NAME, "name")
                .set(Position.POSITION.HUMANREADABLEID, "code")
                .set(Position.POSITION.ORGANIZATION_ID, organizationId)
                .set(Position.POSITION.ORG_STRUCTURE_TYPE, StructureType.INTERNAL)
                .execute();

        var positionId2 = UUID.randomUUID();
        context.insertInto(Position.POSITION)
            .set(Position.POSITION.ID, positionId2)
            .set(Position.POSITION.NAME, "name")
            .set(Position.POSITION.HUMANREADABLEID, "code")
            .set(Position.POSITION.ORGANIZATION_ID, organizationId)
            .set(Position.POSITION.ORG_STRUCTURE_TYPE, StructureType.INTERNAL)
            .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text")
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
                .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text2")
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
                .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text3")
                .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
                .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text4")
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId2)
            .execute();

        var actual = provider.get(positionId);

        assertThat(actual).contains("text", "text2", "text3");
    }

    @Test
    @DisplayName("Получение списка всех типов транспорта")
    void test_getAll() {
        var organizationId = UUID.randomUUID();

        context.insertInto(Organization.ORGANIZATION)
            .set(Organization.ORGANIZATION.ID, organizationId)
            .set(Organization.ORGANIZATION.OFFICIAL_NAME, "name")
            .set(Organization.ORGANIZATION.TIN, "tin")
            .set(Organization.ORGANIZATION.MSRN, "msrn")
            .set(Organization.ORGANIZATION.ADDRESS, "address")
            .execute();

        var positionId = UUID.randomUUID();
        context.insertInto(Position.POSITION)
            .set(Position.POSITION.ID, positionId)
            .set(Position.POSITION.NAME, "name")
            .set(Position.POSITION.HUMANREADABLEID, "code")
            .set(Position.POSITION.ORGANIZATION_ID, organizationId)
            .set(Position.POSITION.ORG_STRUCTURE_TYPE, StructureType.INTERNAL)
            .execute();

        var positionId2 = UUID.randomUUID();
        context.insertInto(Position.POSITION)
            .set(Position.POSITION.ID, positionId2)
            .set(Position.POSITION.NAME, "name")
            .set(Position.POSITION.HUMANREADABLEID, "code")
            .set(Position.POSITION.ORGANIZATION_ID, organizationId)
            .set(Position.POSITION.ORG_STRUCTURE_TYPE, StructureType.INTERNAL)
            .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text")
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
            .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text2")
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
            .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text3")
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId)
            .execute();

        context.insertInto(PositionTaxiClasses.POSITION_TAXI_CLASSES)
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.TAXI_CLASS, "text4")
            .set(PositionTaxiClasses.POSITION_TAXI_CLASSES.POSITION_ID, positionId2)
            .execute();

        var actual = provider.getAll();

        assertThat(actual).contains("text", "text2", "text3", "text4");
    }

}