package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro.DepartmentAvroData;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class DepartmentAvroListener implements Consumer<Message<DepartmentMessage>> {

    private final DepartmentsService departmentsService;

    @Override
    public void accept(Message<DepartmentMessage> raw) {
        departmentsService.createOrUpdate(new DepartmentAvroData(raw.getPayload()));
    }
}
