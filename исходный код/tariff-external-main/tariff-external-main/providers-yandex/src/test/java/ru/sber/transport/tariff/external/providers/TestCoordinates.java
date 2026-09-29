package ru.sber.transport.tariff.external.providers;

import ru.sber.transport.tariff.external.model.Coordinates;

public record TestCoordinates(double latitude, double longitude) implements Coordinates {
}