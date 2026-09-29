package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;
import ru.sberbank.ditsib.corpclient.service.ContactConfirmationService;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactConfirmationServiceImpl implements ContactConfirmationService {

    private final EmployeeProvider employeeProvider;

    private final EmployeeSender employeeSender;

    @Override
    public void confirm(UserDataConfirmationMessage message) {
        var employee = employeeProvider.get(message.getId()).orElse(null);
        if (employee == null) {
            log.warn("Employee not found by id {}", message.getId());
            return;
        }
        if (!Objects.equals(employee.getPhone(), message.getPhone())) {
            log.warn("Phone can not be confirmed, actual phone number is different. Actual: {}, received: {}",
                    employee.getPhone(), message.getPhone());
            return;
        }
        employee.setPhoneConfirmed(true);
        var saved = employeeProvider.save(employee);
        employeeSender.send(saved);
    }
}
