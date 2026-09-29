package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapperImpl;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера делегатов")
class DelegateMapperTest {

    private final DateMapper dateMapper = new DateMapperImpl();

    private final DelegateMapper mapper = new DelegateMapperImpl(new EmployeeMapperImpl(
            new PersonalCarMapperImpl(),
            new AttributeMapperImpl(),
            dateMapper, new ActiveStatusMapperImpl(), new ContactMessageMapperImpl()));
    private final Employee employeeSupervisor = Employee.builder().id(UUID.randomUUID()).build();
    private final Employee employeeDelegate = Employee.builder().id(UUID.randomUUID()).build();

    @Test
    @DisplayName("Запись делегата в сообщение")
    void toMessage() {
        var delegateRecord = DelegateRecord.builder()
                .id(UUID.randomUUID())
                .delegate(employeeDelegate)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusMonths(4).plusDays(5))
                .status(RecordStatus.ACTIVE)
                .supervisor(employeeSupervisor)
                .transportType(TransportTypeEnum.TAXI)
                .build();

        DelegateMessage actual = mapper.toMessage(delegateRecord);
        assertFields(actual, delegateRecord);

        actual = mapper.toMessage(delegateRecord);
        assertFields(actual, delegateRecord);
    }

    @Test
    @DisplayName("Сообщение об удалении делегата")
    void testToMessage() {
        var delegateRecord = DelegateRecord.builder()
                .id(UUID.randomUUID())
                .delegate(employeeDelegate)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusMonths(4).plusDays(5))
                .status(RecordStatus.INACTIVE)
                .supervisor(employeeSupervisor)
                .transportType(TransportTypeEnum.TAXI)
                .build();

        var actual = mapper.toMessage(delegateRecord, true);

        assertThat(actual.getId()).isEqualTo(delegateRecord.getId());
        assertFields(actual, delegateRecord);
        assertThat(actual.isDeleted()).isTrue();
    }

    private void assertFields(DelegateMessage actual, DelegateRecord expected) {
        assertThat(actual.getDelegateId()).isEqualTo(expected.getDelegate().getId());
        assertThat(actual.getStartDate()).isEqualTo(expected.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(expected.getEndDate());
        assertThat(actual.getSupervisorId()).isEqualTo(expected.getSupervisor().getId());
        assertThat(actual.getTransportTypeId()).isEqualTo(expected.getTransportType().getId());
    }
}