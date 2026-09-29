package ru.sber.transport.tariff_fleet.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceType {

    AUTOSERVICE("Обслуживание автомобилей"),
    EMPLOYEE_TRANSPORTATION("Пассажирские перевозки"),
    CARGO_TRANSPORTATION("Перевозки грузов");

    private String rusName;

}
