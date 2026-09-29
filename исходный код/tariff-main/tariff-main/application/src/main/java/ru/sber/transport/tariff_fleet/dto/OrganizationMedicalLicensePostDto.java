package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;

@Schema(name = "OrganizationMedicalLicensePostDto",
        title = "Медицинская лицензия контрагента (создание)",
        description = "Медицинская лицензия контрагента (создание)")
public record OrganizationMedicalLicensePostDto(
        @NotBlank
        @Size(min = 1, max = 50)
        @Schema(description = "Серия",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1", maximum = "50",
                example = "AS3234234")
        String series,
        @NotBlank
        @Size(min = 1, max = 50)
        @Schema(description = "Номер",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1", maximum = "50",
                example = "12421345124DF")
        String number,
        @NotNull
        @Schema(description = "Дата выдачи",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-04-27")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate issueDate,
        @NotNull
        @Schema(description = "Дата окончания срока действия",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-04-27")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate expiryDate
) {
}
