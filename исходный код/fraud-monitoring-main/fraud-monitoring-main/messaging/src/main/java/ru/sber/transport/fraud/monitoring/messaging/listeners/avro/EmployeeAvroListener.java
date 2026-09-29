package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro.EmployeeAvroData;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class EmployeeAvroListener implements Consumer<Message<EmployeeMessage>> {

    private final EmployeesService employeesService;

    @Override
    public void accept(Message<EmployeeMessage> raw) {
        employeesService.createOrUpdate(new EmployeeAvroData(raw.getPayload()));
    }
}
