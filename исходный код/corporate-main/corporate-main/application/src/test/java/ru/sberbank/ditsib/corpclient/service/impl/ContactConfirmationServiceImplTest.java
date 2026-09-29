package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@ExtendWith(MockitoExtension.class)
@Feature("app_platform_corporate")
class ContactConfirmationServiceImplTest {

    @InjectMocks
    private ContactConfirmationServiceImpl contactConfirmationService;

    @Mock
    private EmployeeProvider employeeProvider;

    @Mock
    private EmployeeSender employeeSender;

    @Test
    @DisplayName("Подтверждение")
    void testConfirm_Success() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);

        Employee employee = Instancio.of(Employee.class)
                .set(field(Employee::isPhoneConfirmed), false)
                .set(field(Employee::getPhone), message.getPhone())
                .create();

        when(employeeProvider.get(message.getId()))
                .thenReturn(Optional.of(employee));

        when(employeeProvider.save(argThat(Employee::isPhoneConfirmed)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(employeeSender, times(1))
                .send(argThat(Employee::isPhoneConfirmed));
    }

    @Test
    @DisplayName("Подтверждение. Сотрудник не найден")
    void testConfirm_Error_NoEmployee() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);

        when(employeeProvider.get(message.getId()))
                .thenReturn(Optional.empty());

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(employeeProvider, never())
                .save(any());

        verify(employeeSender, never())
                .send(any());
    }

    @Test
    @DisplayName("Подтверждение. Разные телефоны")
    void testConfirm_Error_DifferentPhones() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);

        Employee employee = Instancio.of(Employee.class)
                .set(field(Employee::isPhoneConfirmed), false)
                .create();

        when(employeeProvider.get(message.getId()))
                .thenReturn(Optional.of(employee));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(employeeProvider, never())
                .save(any());

        verify(employeeSender, never())
                .send(any());
    }
}