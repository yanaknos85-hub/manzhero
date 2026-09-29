package ru.sber.transport.tariff_fleet.validation;

import java.util.List;

public record ExcelValidationResult<T>(
        boolean hasAnyError,
        List<T> enrtyList
) {
}
