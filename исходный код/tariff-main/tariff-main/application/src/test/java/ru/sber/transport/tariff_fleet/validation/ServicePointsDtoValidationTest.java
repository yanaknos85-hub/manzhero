package ru.sber.transport.tariff_fleet.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ServicePointsDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    void validateServiceStationDto(String testCase, ServicePointDto dto, String message) {
        var violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .hasSize(1);
        assertThat(message).isEqualTo(
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .findFirst()
                        .orElseThrow(() -> new JUnitException("ConstraintViolation is empty"))
        );
    }

    static Stream<Arguments> validateServiceStationDto() {
        return Stream.of(
                Arguments.of(
                        "address is null",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::address), null)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Адрес не может быть пустым"
                ),
                Arguments.of(
                        "address is empty",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::address), "          ")
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Адрес не может быть пустым"
                ),
                Arguments.of(
                        "address length more than 70",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::address),
                                        "ул. Супердлиннаяназваннаялиния, д. 1234567890, кв. 9876543210, г. Мегаполис")
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Адрес должен быть от 1 до 70 символов"
                ),
                Arguments.of(
                        "latitude is null",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), null)
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Широта не может быть пустой"
                ),
                Arguments.of(
                        "latitude more than 90",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(90.00000001D))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Широта не может быть больше 90"
                ),
                Arguments.of(
                        "latitude less than -90",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(-90.00000001D))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(0))
                                .create(),
                        "Широта не может быть меньше -90"
                ),
                Arguments.of(
                        "longitude is null",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), null)
                                .create(),
                        "Долгота не может быть пустой"
                ),
                Arguments.of(
                        "longitude more than 180",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(180.00000001D))
                                .create(),
                        "Долгота не может быть больше 180"
                ),
                Arguments.of(
                        "longitude less than -180",
                        Instancio.of(ServicePointDto.class)
                                .set(Select.field(ServicePointDto::latitude), BigDecimal.valueOf(0))
                                .set(Select.field(ServicePointDto::longitude), BigDecimal.valueOf(-180.00000001D))
                                .create(),
                        "Долгота не может быть меньше -180"
                )
        );
    }
}

