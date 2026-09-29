package ru.sber.transport.tariff_fleet.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ValidatedServicePointDto;
import ru.sber.transport.tariff_fleet.mapper.ServicePointMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.sber.transport.tariff_fleet.exception.ServicePointsValidationException.*;
import static ru.sber.transport.tariff_fleet.service.validation.impl.ServicePointValidationServiceImpl.MAX_COORDINATE_SCALE;

@Service
@RequiredArgsConstructor
public class ServicePointExcelDtoValidator {

    private final ServicePointMapper mapper;

    private final HashMap<String, Integer> coordinatesHashMap = new HashMap<>();
    private boolean hasAnyError = false;

    public ExcelValidationResult<ValidatedServicePointDto> validateAndMap(List<ServicePointExcelDto> source) {
        coordinatesHashMap.clear();
        hasAnyError = false;

        var validated = source.stream()
                .map(this::validateAndMap)
                .toList();

        return new ExcelValidationResult<>(hasAnyError, validated);
    }

    private ValidatedServicePointDto validateAndMap(ServicePointExcelDto source) {
        var errorMessage = validateInputData(source);
        return mapper.servicePointExcelDtoToValidatedServicePointDto(source, errorMessage);
    }

    private String validateInputData(ServicePointExcelDto value) {
        var errors = new ArrayList<String>();

        errors.add(notEmptyFields(value));
        errors.add(boundaryCheck(value));
        errors.add(coordinatesUniqueCheck(value));
        errors.add(validateServicePointCoordinates(value.getLatitude(), value.getLongitude()));

        var result = errors.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(". "));
        if (result.isEmpty()) {
            return null;
        } else {
            hasAnyError = true;
            return result;
        }
    }

    private String notEmptyFields(ServicePointExcelDto value) {
        var emptyFields = new ArrayList<String>();

        if (isEmpty(String.valueOf(value.getNumber()))) {
            emptyFields.add("Номер по порядку");
        }
        if (isEmpty(value.getAddress())) {
            emptyFields.add("Адрес точки обслуживания");
        }
        if (isEmpty(String.valueOf(value.getLatitude()))) {
            emptyFields.add("Широта");
        }
        if (isEmpty(String.valueOf(value.getLongitude()))) {
            emptyFields.add("Долгота");
        }

        if (!emptyFields.isEmpty()) {
            return String.format("Не заполнены поля: %s", emptyFields);
        } else {
            return "";
        }
    }

    private String boundaryCheck(ServicePointExcelDto value) {
        var errors = new ArrayList<String>();

        errors.add(maxSize("Номер по порядку", String.valueOf(value.getNumber()), 4));
        errors.add(maxSize("Адрес точки обслуживания", value.getAddress(), 70));

        var result = errors.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(". "));
        return result.isEmpty() ? "" : result;
    }

    private String coordinatesUniqueCheck(ServicePointExcelDto value) {
        if (value.getLatitude() == null || value.getLongitude() == null) {
            return "";
        }

        var coordinates = value.getLatitude() + value.getLongitude().toString();
        var duplicateNumber = coordinatesHashMap.get(coordinates);
        if (duplicateNumber != null) {
            return String.format("Дубликат строки %d по широте и долготе", duplicateNumber);
        } else {
            coordinatesHashMap.put(coordinates, value.getNumber());
            return "";
        }
    }

    private String validateServicePointCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return "";
        }

        var errors = new ArrayList<String>();

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            errors.add(INVALID_LATITUDE_VALUE_MSG);
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            errors.add(INVALID_LONGITUDE_VALUE_MSG);
        }

        if (latitude.scale() > MAX_COORDINATE_SCALE) {
            errors.add(String.format(INVALID_LATITUDE_SCALE_VALUE_MSG, MAX_COORDINATE_SCALE));
        }

        if (longitude.scale() > MAX_COORDINATE_SCALE) {
            errors.add(String.format(INVALID_LONGITUDE_SCALE_VALUE_MSG, MAX_COORDINATE_SCALE));
        }

        var result = errors.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(". "));
        return result.isEmpty() ? "" : result;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty() || Objects.equals(value, "null");
    }

    private String maxSize(String fieldName, String value, int maxLimit) {
        if (value != null && value.length() > maxLimit) {
            return String.format("Превышено максимально возможное количество символов в поле: %s, ожидается: %d", fieldName, maxLimit);
        } else {
            return "";
        }
    }

}
