package ru.sber.transport.fraud.monitoring.business.model;


import ru.sber.transport.fraud.monitoring.model.Position;

import java.util.UUID;

public record TestPosition(UUID getId, String getName) implements Position {
}
