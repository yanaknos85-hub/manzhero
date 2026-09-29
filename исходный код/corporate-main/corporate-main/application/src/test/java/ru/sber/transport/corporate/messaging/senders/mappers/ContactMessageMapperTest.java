package ru.sber.transport.corporate.messaging.senders.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.messages.corporate.avro.ContactEmployeeType;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка работы маппера контактов сотрудников")
class ContactMessageMapperTest {

    private final ContactMessageMapper mapper = new ContactMessageMapperImpl();

    @Test
    @DisplayName("Проверка работы маппера контактов сотрудников")
    void test_map() {
        final var source = Instancio.create(Employee.class);

        final var contacts = mapper.toMessage(source);

        assertThat(mapper.toMessage((Employee)null)).isEmpty();

        assertThat(contacts.getFirst().getInternal()).isFalse();
        assertThat(contacts.get(0).getType()).isEqualTo(ContactEmployeeType.MOBILE);
        assertThat(contacts.get(0).getValue()).isEqualTo(source.getPhone());

        assertThat(contacts.get(1).getInternal()).isTrue();
        assertThat(contacts.get(1).getType()).isEqualTo(ContactEmployeeType.EMAIL);
        assertThat(contacts.get(1).getValue()).isEqualTo(source.getEmail());

        assertThat(contacts.get(2).getInternal()).isFalse();
        assertThat(contacts.get(2).getType()).isEqualTo(ContactEmployeeType.EMAIL);
        assertThat(contacts.get(2).getValue()).isEqualTo(source.getExternalEmail());
    }

    @Test
    @DisplayName("Проверка работы маппера контактов сотрудников old employee")
    void test_map_old_employee() {
        final var source = Instancio.create(ru.sberbank.ditsib.corpclient.database.model.Employee.class);

        final var contacts = mapper.toMessage(source);

        assertThat(mapper.toMessage((ru.sberbank.ditsib.corpclient.database.model.Employee)null)).isEmpty();

        assertThat(contacts.getFirst().getInternal()).isFalse();
        assertThat(contacts.get(0).getType()).isEqualTo(ContactEmployeeType.MOBILE);
        assertThat(contacts.get(0).getValue()).isEqualTo(source.getMobilePhone());

        assertThat(contacts.get(1).getInternal()).isTrue();
        assertThat(contacts.get(1).getType()).isEqualTo(ContactEmployeeType.EMAIL);
        assertThat(contacts.get(1).getValue()).isEqualTo(source.getEmail());

        assertThat(contacts.get(2).getInternal()).isFalse();
        assertThat(contacts.get(2).getType()).isEqualTo(ContactEmployeeType.EMAIL);
        assertThat(contacts.get(2).getValue()).isEqualTo(source.getExternalEmail());
    }

}