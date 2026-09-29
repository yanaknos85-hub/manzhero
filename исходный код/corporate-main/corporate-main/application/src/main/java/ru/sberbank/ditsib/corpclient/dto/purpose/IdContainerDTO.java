package ru.sberbank.ditsib.corpclient.dto.purpose;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Jacksonized
@Builder
@Data
@Schema(title = "Идентификатор", description = "Объект для идентификатора")
public class IdContainerDTO {
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
}
