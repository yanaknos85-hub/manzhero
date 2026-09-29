package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.DepartmentData;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class DepartmentListener implements Consumer<Message<DepartmentMessage>> {

    private final DepartmentsService departmentsService;

    @Override
    public void accept(Message<DepartmentMessage> raw) {
        departmentsService.createOrUpdate(new DepartmentData(raw.getPayload()));
    }
}
