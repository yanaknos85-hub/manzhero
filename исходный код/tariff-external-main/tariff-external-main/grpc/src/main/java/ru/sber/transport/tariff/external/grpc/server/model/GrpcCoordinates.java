package ru.sber.transport.tariff.external.grpc.server.model;

import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.model.Coordinates;

/**
 * gRPC Координаты
 *
 * @param latitude широта
 * @param longitude долгота
 */
public record GrpcCoordinates(double latitude, double longitude) implements Coordinates {

    /**
     * Конструктор для создания экземпляра
     *
     * @param coordinates исходные данные
     */
    public GrpcCoordinates(ExternalTariff.Coordinates coordinates) {
        this(coordinates.getLatitude(), coordinates.getLongitude());
    }

}
