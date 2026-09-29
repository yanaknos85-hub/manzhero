package ru.sber.transport.fraud.monitoring.business.model;


import ru.sber.transport.fraud.monitoring.model.Department;

import java.util.UUID;

public record TestDepartment(UUID getId, UUID getHeadId, UUID getParentId, String getName,
                             String getCode) implements Department {
}
