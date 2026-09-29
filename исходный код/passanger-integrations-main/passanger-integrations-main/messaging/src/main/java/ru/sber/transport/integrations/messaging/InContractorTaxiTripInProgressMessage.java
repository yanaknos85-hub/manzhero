package ru.sber.transport.integrations.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.LocalDateTime;


/**
 * Актуальная информация о поездке на такси от контрагента в состоянии In progress
 *
 * @param tripId               ID в системе Банка
 * @param humanId              Человекочитаемый ID в системе Банка
 * @param taxiId               ID в системе Исполнителя
 * @param status               Код статуса из системы Исполнителя
 * @param resolution           Решение (Содержит информацию о водителе (ФИО, телефон) и об автомобиле (марка, цвет, регистрационный номер))
 * @param contractorEmail      Почта, с которой было отправлено сообщение об этой поездке
 * @param integrationType      Тип интеграции.
 * @param driver               Водитель
 * @param vehicle              Транспортное средство
 * @param geoLocation          Последнее местоположение автомобиля
 * @param bearing
 * @param geoTime              Время последнего местоположения автомобиля
 * @param performerArrivalTime Время подачи транспортного средства (прибытия водителя в точку отправления)
 * @param createOrderTime      Дата создания заказа
 * @param collectionTime       Дата начала поездки
 * @param price                Стоимость заказа, коп.
 * @param distance             Преодолённая дистанция, км
 * @param finishTime           Дата закрытия заказа
 * @param comment              Комментарий
 * @param eta                  Примерное время до прибытия машины или до окончания поездки, мин
 * @param isTest               Признак тестового заказа
 * @param waitTime             Время ожидания, мин
 * @param waitTimeOW           Время ожидания в пути, мин
 * @param transportType        Тип транспорта
 */
public record InContractorTaxiTripInProgressMessage(
        @NotNull
        String tripId,
        String humanId,
        @NotBlank
        String taxiId,
        @NotNull
        InboundTaxiTripStatus status,
        @NotBlank
        String resolution,
        String contractorEmail,
        TaxiExternalIntegrationType integrationType,
        Driver driver,
        Vehicle vehicle,
        GeoPointDTO geoLocation,
        Double bearing,
        LocalDateTime geoTime,
        LocalDateTime performerArrivalTime,
        LocalDateTime createOrderTime,
        LocalDateTime collectionTime,
        Double price,
        Double distance,
        LocalDateTime finishTime,
        String comment,
        Integer eta,
        Boolean isTest,
        Integer waitTime,
        Integer waitTimeOW,
        TransportTypeEnum transportType
) implements Message<String> {

    @JsonIgnore
    @Override
    public String getId() {
        return tripId;
    }

    /**
     * Model of a driver.
     *
     * @param id         ID of the driver.
     * @param name       first name of the driver.
     * @param patronymic patronymic (middle name) of the driver.
     * @param secName    last name of the driver.
     * @param phone      phone of the driver.
     * @param imageUrl   imageUrl of the driver.
     * @param rating     rating of the driver.
     * @param companyId  companyId of the driver.
     */
    public record Driver(
            String id,
            String name,
            String patronymic,
            String secName,
            String phone,
            String imageUrl,
            String rating,
            String companyId
    ) {
    }

    /**
     * A vehicle data model.
     *
     * @param mark               brand of the vehicle.
     * @param model              model of the vehicle.
     * @param color              color of the vehicle.
     * @param registrationNumber registrationNumber of the vehicle.
     */
    public record Vehicle(
            String mark,
            String model,
            String color,
            String registrationNumber
    ) {
    }
}
