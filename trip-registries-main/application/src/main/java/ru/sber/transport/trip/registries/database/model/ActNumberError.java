package ru.sber.transport.trip.registries.database.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Доступные типы согласований.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ActNumberError {

    P001("Ошибка на Портале Поставщика"),

    T002("Ошибка на стороне АС СберТранспорт"),

    T012("Рамочный договор не найден"),

    T015("Акт по реестру не найден");

    private final String description;
}
