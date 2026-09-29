package ru.sber.transport.tariff_fleet.dto.service_point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.With;

import java.math.BigDecimal;
import java.util.Objects;

@With
@Schema(name = "ServicePoint", description = "Данные о сервисной станции")
public record ServicePointDto(

        @NotBlank(message = "Адрес не может быть пустым")
        @Size(max = 70, message = "Адрес должен быть от 1 до 70 символов")
        @Schema(description = "Адрес",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1", maximum = "70",
                example = "г. Москва, ул. Ленина, д. 123")
        String address,
        
        @NotNull(message = "Широта не может быть пустой")
        @Max(value = 90, message = "Широта не может быть больше 90")
        @Min(value = -90, message = "Широта не может быть меньше -90")
        @Schema(description = "Широта",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maximum = "90",
                minimum = "-90",
                example = "55.755826")
        BigDecimal latitude,
        
        @NotNull(message = "Долгота не может быть пустой")
        @Max(value = 180, message = "Долгота не может быть больше 180")
        @Min(value = -180, message = "Долгота не может быть меньше -180")
        @Schema(description = "Долгота",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maximum = "180",
                minimum = "-180",
                example = "37.617298")
        BigDecimal longitude
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var servicePoint = (ServicePointDto) o;
        return Objects.equals(latitude, servicePoint.latitude()) &&
               Objects.equals(longitude, servicePoint.longitude()) &&
               Objects.equals(address, servicePoint.address());
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(address, latitude, longitude);
        result = 31 * result;
        return result;
    }
    
    @Override
    public String toString() {
        return "ServicePoint{" +
               ", address='" + address + '\'' +
               ", latitude=" + latitude +
               ", longitude=" + longitude +
               '}';
    }
}
