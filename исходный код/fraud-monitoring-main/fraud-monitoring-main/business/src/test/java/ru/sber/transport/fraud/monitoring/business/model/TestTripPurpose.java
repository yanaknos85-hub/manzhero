package ru.sber.transport.fraud.monitoring.business.model;


import ru.sber.transport.fraud.monitoring.model.TripPurpose;

import java.util.UUID;

public record TestTripPurpose(UUID getId, String getLabel) implements TripPurpose {
}
