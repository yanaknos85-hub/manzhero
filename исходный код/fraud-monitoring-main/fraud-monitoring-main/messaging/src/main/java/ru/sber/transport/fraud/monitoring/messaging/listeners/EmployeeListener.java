package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.EmployeeData;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class EmployeeListener implements Consumer<Message<EmployeeMessage>> {

    private final EmployeesService employeesService;

    @Override
    public void accept(Message<EmployeeMessage> raw) {
        employeesService.createOrUpdate(new EmployeeData(raw.getPayload()));
    }
}
