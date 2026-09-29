package ru.sber.transport.fraud.monitoring.providers.model;


import ru.sber.transport.fraud.monitoring.model.Organization;

import java.util.UUID;

public record TestOrganization(UUID getId, long getDigitId) implements Organization {
}