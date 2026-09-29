package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Признаки сотрудника у цели поездки", description = "Фильтры цели поездки")
public class TripPurposeAttributeDTO {
    @JsonIgnore
    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "Признаки сотрудника")
    private AttributeDto attribute;
}
