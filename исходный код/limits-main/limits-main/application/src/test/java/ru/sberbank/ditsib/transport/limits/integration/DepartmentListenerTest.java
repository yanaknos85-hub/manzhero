package ru.sberbank.ditsib.transport.limits.integration;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.instancio.Select.field;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class DepartmentListenerTest extends KafkaTest {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepLimitRepository depLimitRepository;

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/limit.sql"})
    void listen() {
        var departmentMessage1 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::isDeleted), false)
                .set(field(DepartmentMessage::getOrganizationId), UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .set(field(DepartmentMessage::getDepartmentHeadId), null)
                .set(field(DepartmentMessage::getParentId), null)
                .create();
        var departmentMessage2 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::isDeleted), true)
                .set(field(DepartmentMessage::getId), UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"))
                .create();
        var departmentMessage3 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::isDeleted), true)
                .set(field(DepartmentMessage::getId), UUID.fromString("e4cdf1e8-45d6-4ecf-8330-6b10760d256d"))
                .create();
        var departmentMessage4 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::isDeleted), true)
                .set(field(DepartmentMessage::getId), UUID.fromString("c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d"))
                .create();
        var departmentMessage5 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::isDeleted), true)
                .set(field(DepartmentMessage::getId), UUID.fromString("79fb1427-6e87-4755-bdde-fb3434d1bbd2"))
                .create();
        assertThatExceptionOfType(LimitLogicException.class)
                .isThrownBy(() -> produceMessage("service.organization.department", departmentMessage2))
                .withMessage("ERROR: Попытка удаления подразделения с действующими дочерними лимитами!");
        produceMessage("service.organization.department", departmentMessage1);
        produceMessage("service.organization.department", departmentMessage3);
        assertThat(departmentRepository.findAll())
                .hasSize(7)
                .extracting(Department::getId, Department::isActive)
                .containsExactlyInAnyOrder(
                        tuple(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"), true),
                        tuple(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"), true),
                        tuple(UUID.fromString("c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d"), true),
                        tuple(UUID.fromString("21f90644-fb63-4225-a794-c6062ad53e56"), true),
                        tuple(UUID.fromString("e4cdf1e8-45d6-4ecf-8330-6b10760d256d"), false),
                        tuple(UUID.fromString("79fb1427-6e87-4755-bdde-fb3434d1bbd2"), true),
                        tuple(departmentMessage1.getId(), true));
        assertThat(depLimitRepository.findAll())
                .hasSize(4)
                .extracting(DepLimit::getId,
                        DepLimit::getLimitStatus,
                        depLimit -> depLimit.getSum().setScale(0, RoundingMode.HALF_UP))
                .containsExactlyInAnyOrder(
                        tuple(
                                UUID.fromString("9f236cc6-d00a-4eea-acf3-07dc23bfe2ed"),
                                LimitStatus.SHARED,
                                BigDecimal.valueOf(1000000)),
                        tuple(
                                UUID.fromString("585df74d-c5e1-4452-9fcf-8e2202bb8106"),
                                LimitStatus.SHARED,
                                BigDecimal.valueOf(1000000)),
                        tuple(
                                UUID.fromString("0c21f83d-c417-4b07-98e9-fec8a4f2495c"),
                                LimitStatus.SHARED,
                                BigDecimal.valueOf(1000000)),
                        tuple(
                                UUID.fromString("d4390f6a-4f45-4b69-8553-a9cc6d26bf1a"),
                                LimitStatus.CLOSED,
                                BigDecimal.valueOf(1000000))
                );
        assertThatExceptionOfType(LimitLogicException.class)
                .isThrownBy(() -> produceMessage("service.organization.department", departmentMessage4))
                .withMessage("ERROR: Попытка удаления подразделения с действующими дочерними лимитами!");
        produceMessage("service.organization.department", departmentMessage5);
        assertThat(departmentRepository.findAll())
                .hasSize(7)
                .extracting(Department::getId, Department::isActive)
                .containsExactlyInAnyOrder(
                        tuple(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"), true),
                        tuple(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"), true),
                        tuple(UUID.fromString("c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d"), true),
                        tuple(UUID.fromString("21f90644-fb63-4225-a794-c6062ad53e56"), true),
                        tuple(UUID.fromString("e4cdf1e8-45d6-4ecf-8330-6b10760d256d"), false),
                        tuple(UUID.fromString("79fb1427-6e87-4755-bdde-fb3434d1bbd2"), false),
                        tuple(departmentMessage1.getId(), true));
        assertThat(depLimitRepository.findAll())
                .hasSize(4)
                .extracting(DepLimit::getId,
                        DepLimit::getLimitStatus,
                        depLimit -> depLimit.getSum().setScale(0, RoundingMode.HALF_UP))
                .containsExactlyInAnyOrder(
                        tuple(
                                UUID.fromString("9f236cc6-d00a-4eea-acf3-07dc23bfe2ed"),
                                LimitStatus.SHARED,
                                BigDecimal.valueOf(1000000)),
                        tuple(
                                UUID.fromString("585df74d-c5e1-4452-9fcf-8e2202bb8106"),
                                LimitStatus.SHARED,
                                BigDecimal.valueOf(40999996)),
                        tuple(
                                UUID.fromString("0c21f83d-c417-4b07-98e9-fec8a4f2495c"),
                                LimitStatus.CLOSED,
                                BigDecimal.valueOf(-38999996)),
                        tuple(
                                UUID.fromString("d4390f6a-4f45-4b69-8553-a9cc6d26bf1a"),
                                LimitStatus.CLOSED,
                                BigDecimal.valueOf(1000000))
                );
    }
}