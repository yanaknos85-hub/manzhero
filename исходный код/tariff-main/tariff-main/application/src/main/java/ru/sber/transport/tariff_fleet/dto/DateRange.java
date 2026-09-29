package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.LocalDate;

@Schema(name = "DateRange", title = "Диапазон дат", description = "Диапазон дат")
@Interval(startField = "start", endField = "end", inclusion = Interval.Include.INCLUDE)
public record DateRange(
        @NotNull
        @Schema(description = "Начало периода",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-04-27")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate start,
        @NotNull
        @Schema(description = "Окончание периода",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-04-27")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate end
) {
}