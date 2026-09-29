package ru.sber.transport.integrations.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @param childSeat Детское кресло
 * @param childSeatDetails Детское кресло
 * @param bugs Количество багажа
 * @param bugsComment Количество багажа, комментарий
 * @param bugsOversized Негабаритный багаж
 * @param bugsOversizedComment Негабаритный багаж, комментарий
 * @param animal Животные
 * @param animalComment Животные, комментарий
 * @param addContactFIO Дополнительное контактное лицо фио
 * @param addContactPhone Дополнительное контактное лицо телефон
 * @param numberFlight Номер рейса/поезда:  текст
 * @param dateFlight Дата и время рейса/поездка
 * @param phoneHotel Телефон принимающей гостиницы
 * @param typeVehicle Желаемый тип Транспортного средства
 * @param transportId Ид машины
 */
public record GroupTransferRequestInformation(
        boolean childSeat,
        ChildSeatDetails childSeatDetails,
        boolean bugs,
        String bugsComment,
        boolean bugsOversized,
        String bugsOversizedComment,
        boolean animal,
        String animalComment,
        String addContactFIO,
        String addContactPhone,
        String numberFlight,
        OffsetDateTime dateFlight,
        String phoneHotel,
        String typeVehicle,
        UUID transportId
) {
}