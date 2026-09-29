package ru.sber.transport.tariff_fleet.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.config.ServicePointsConfig;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.ServicePointsAbsentException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsUniqueException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsValidationException;
import ru.sber.transport.tariff_fleet.exception.TooManyServicePointsException;
import ru.sber.transport.tariff_fleet.service.validation.impl.ServicePointValidationServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ServicePointValidationServiceImplTest {

    @InjectMocks
    private ServicePointValidationServiceImpl servicePointValidationService;

    @Mock
    private ServicePointsConfig servicePointsConfig;

    @BeforeEach
    void setUp() {
        doReturn(10).when(servicePointsConfig).maxRowCount();
    }

    @Test
    @DisplayName("Валидные точки обслуживания")
    void validateServicePoint() {
        var validServicePoints = List.of(
                new ServicePointDto("SP1", new BigDecimal("55.75"), new BigDecimal("37.6")),
                new ServicePointDto("SP2", new BigDecimal("56.1"), new BigDecimal("38.0"))
        );
        assertDoesNotThrow(() -> servicePointValidationService.validateServicePoint(validServicePoints));
    }

    @Test
    @DisplayName("Превышение максимального количества точек обслуживания")
    void testTooManyServicePoints() {
        var tooManyServicePoints = Collections.nCopies(11, new ServicePointDto("SP", new BigDecimal("55.75"), new BigDecimal("37.6")));
        assertThrows(TooManyServicePointsException.class, () -> servicePointValidationService.validateServicePoint(tooManyServicePoints));
    }

    @Test
    @DisplayName("Дублирующиеся точки обслуживания")
    void testNonUniqueServicePoints() {
        var nonUniqueServicePoints = List.of(
                new ServicePointDto("SP1", new BigDecimal("55.75"), new BigDecimal("37.6")),
                new ServicePointDto("SP1", new BigDecimal("55.75"), new BigDecimal("37.6"))
        );
        assertThrows(ServicePointsUniqueException.class, () -> servicePointValidationService.validateServicePoint(nonUniqueServicePoints));
    }

    @Test
    @DisplayName("Некорректная широта")
    void testInvalidLatitude() {
        var invalidLatScaleServicePoints = List.of(new ServicePointDto("SP1", new BigDecimal("90.0001"), new BigDecimal("37.6")));
        assertThrows(ServicePointsValidationException.class, () -> servicePointValidationService.validateServicePoint(invalidLatScaleServicePoints));
    }

    @Test
    @DisplayName("Некорректная долгота")
    void testInvalidLongitude() {
        var invalidLonScaleServicePoints = List.of(new ServicePointDto("SP1", new BigDecimal("55.75"), new BigDecimal("180.0001")));
        assertThrows(ServicePointsValidationException.class, () -> servicePointValidationService.validateServicePoint(invalidLonScaleServicePoints));
    }

    @Test
    @DisplayName("Некорректная точность широты")
    void testInvalidLatitudeScale() {
        var invalidLatScaleServicePoints = List.of(new ServicePointDto("SP1", new BigDecimal("55.1234567"), new BigDecimal("37.6")));
        assertThrows(ServicePointsValidationException.class, () -> servicePointValidationService.validateServicePoint(invalidLatScaleServicePoints));
    }

    @Test
    @DisplayName("Некорректная точность долготы")
    void testInvalidLongitudeScale() {
        var invalidLonScaleServicePoints = List.of(new ServicePointDto("SP1", new BigDecimal("55.75"), new BigDecimal("37.1234567")));
        assertThrows(ServicePointsValidationException.class, () -> servicePointValidationService.validateServicePoint(invalidLonScaleServicePoints));
    }

    @Test
    @DisplayName("Отсутствие координат")
    void testMissingCoordinates() {
        var missingCoordinatesServicePoints = List.of(new ServicePointDto("SP1", null, null));
        assertThrows(ServicePointsAbsentException.class, () -> servicePointValidationService.validateServicePoint(missingCoordinatesServicePoints));
    }

}
