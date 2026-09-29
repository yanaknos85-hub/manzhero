package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.fraud.monitoring.model.Fraud;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FraudImpl implements Fraud {

    private UUID id;
    private UUID requestId;
    private String comment;
    private String fraudType;
    private String source;
}
