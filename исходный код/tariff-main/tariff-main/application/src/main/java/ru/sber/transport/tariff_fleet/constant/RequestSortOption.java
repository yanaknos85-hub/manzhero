package ru.sber.transport.tariff_fleet.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы сортировок
 */
@Getter
@RequiredArgsConstructor
public enum RequestSortOption {

    HUMAN_READABLE_ID("Человекочитаемый идентификатор");
    private final String description;
}
