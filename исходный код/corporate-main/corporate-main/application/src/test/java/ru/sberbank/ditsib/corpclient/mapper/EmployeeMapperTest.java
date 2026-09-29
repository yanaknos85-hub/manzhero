package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapperImpl;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.transport.constants.ItinerantType;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера сотрудников")
class EmployeeMapperTest {

    private final EmployeeMapper employeeMapper = new EmployeeMapperImpl(
            new PersonalCarMapperImpl(), new AttributeMapperImpl(),
            new DateMapperImpl(), new ActiveStatusMapperImpl(), new ContactMessageMapperImpl()
    );

    @Test
    @DisplayName("Проверка работы маппера старого класса сотрудников")
    void testEmployeeMapper() {
        var e = Instancio.create(Employee.class);
        EmployeeMessage messageAvro = employeeMapper.toMessageAvro(e);

        assertThat(messageAvro)
                .isNotNull();

        assertThat(messageAvro.getContacts())
                .hasSize(3);
    }

    @Test
    @DisplayName("Проверка маппинга сотрудника из сообщения grpc")
    void test_fromEmployeeResponse() {
        var source = State.Employee.newBuilder()
                .setPersonnelNumber("091412")
                .setOrganizationId(UUID.randomUUID().toString())
                .setDepartmentId(UUID.randomUUID().toString())
                .setPositionId(UUID.randomUUID().toString())
                .setAddress(State.NullableString.newBuilder().setValue("Address").build())
                .setGender(State.Gender.MALE)
                .setFirstName("Имя")
                .setLastName("Фамилия")
                .setPatronymic(State.NullableString.newBuilder().setValue("Отчество").build())
                .setActive(true)
                .setConsent(true)
                .setCostCenter(State.NullableString.newBuilder().setValue("CostCenter").build())
                .setEmail(State.NullableString.newBuilder().setValue("Email").build())
                .setExternalEmail(State.NullableString.newBuilder().setValue("ExternalEmail").build())
                .setFireDate(State.NullableDate.newBuilder().setValue(State.Date.newBuilder().setYear(2024).setMonth(2).setDay(3).build()).build())
                .setUpdateDate(State.OffsetDateTime.newBuilder().setDate(State.Date.newBuilder().setYear(2024).setMonth(2).setDay(3).build()).setHour(1).setMinute(2).setSecond(3).setMillis(4).setOffset(ZoneOffset.UTC.getId()).build())
                .setItinerantType(State.ItinerantType.NONE)
                .build();

        var target = new Employee();

        employeeMapper.update(target, source);

        assertSoftly(it -> {
            it.assertThat(target.getFirstName()).isEqualTo(source.getFirstName());
            it.assertThat(target.getLastName()).isEqualTo(source.getLastName());
            it.assertThat(target.getItinerantType().name()).isEqualTo(source.getItinerantType().name());
            it.assertThat(target.getEmail()).isEqualTo(source.getEmail().getValue());
            it.assertThat(target.getFireDate().getYear()).isEqualTo(source.getFireDate().getValue().getYear());
            it.assertThat(target.getFireDate().getMonth().getValue()).isEqualTo(source.getFireDate().getValue().getMonth());
            it.assertThat(target.getFireDate().getDayOfMonth()).isEqualTo(source.getFireDate().getValue().getDay());
        });
    }

    @Test
    @DisplayName("Проверка маппинга сотрудника для сообщения кафки")
    void test_toMessage() {

        var userId = UUID.randomUUID();
        Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        var expected = Employee.builder()
                .id(userId)
                .isNew(true)
                .userId(userId)
                .firstName("Василий")
                .lastName("Сусветов")
                .patronymic("Федотович")
                .gender(Gender.MALE)
                .email("test@e.mail")
                .department(Department.builder()
                        .id(UUID.randomUUID())
                        .organization(organization)
                        .build())
                .position(Position.builder().id(UUID.randomUUID()).build())
                .organization(organization)
                .supervisor(Employee.builder().id(UUID.randomUUID()).build())
                .marriageCertificateId("Marriage001")
                .humanReadableId("HRID1")
                .personnelNumber("PN01")
                .costCenter("testCostCenter")
                .itinerantType(ItinerantType.PARTIAL)
                .mobilePhone("+78336162514")
                .phoneConfirmed(true)
                .consent(false)
                .build();

        var actual = employeeMapper.toMessage(expected, organization, expected.getAttributes().stream().map(Attribute::getName).collect(Collectors.toUnmodifiableSet()));

        assertAll(
                () -> assertEquals(expected.getPosition().getId(), actual.getPositionId()),
                () -> assertEquals(expected.getSupervisor().getId(), actual.getSupervisorId()),
                () -> assertEquals(expected.getDepartment().getId(), actual.getDepartmentId()),
                () -> assertEquals(expected.getMarriageCertificateId(), actual.getMarriageCertificateNumber()),
                () -> assertEquals(expected.getDepartment().getOrganization().getId(), actual.getOrganizationId()),
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getUserId(), actual.getUserId()),
                () -> assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId()),
                () -> assertEquals(expected.getFirstName(), actual.getFirstName()),
                () -> assertEquals(expected.getLastName(), actual.getLastName()),
                () -> assertEquals(expected.getPatronymic(), actual.getPatronymic()),
                () -> assertEquals(expected.getPersonnelNumber(), actual.getPersonnelNumber()),
                () -> assertEquals(expected.getCostCenter(), actual.getCostCenter()),
                () -> assertEquals(expected.getItinerantType().name(), actual.getItinerantType()),
                () -> assertEquals(expected.getMobilePhone(), actual.getMobilePhone()),
                () -> assertEquals(expected.getEmail(), actual.getEmail()),
                () -> assertEquals(expected.getGender().name(), actual.getGender()),
                () -> assertEquals(expected.isConsent(), actual.isConsent())
        );
    }
}
