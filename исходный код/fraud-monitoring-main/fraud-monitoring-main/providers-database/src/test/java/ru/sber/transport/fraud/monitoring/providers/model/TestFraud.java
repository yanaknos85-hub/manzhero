package ru.sber.transport.fraud.monitoring.providers.model;


import ru.sber.transport.fraud.monitoring.model.Fraud;

import java.util.UUID;

public record TestFraud(UUID getId, UUID getRequestId, String getComment, String getFraudType, String getSource) implements Fraud {
}
