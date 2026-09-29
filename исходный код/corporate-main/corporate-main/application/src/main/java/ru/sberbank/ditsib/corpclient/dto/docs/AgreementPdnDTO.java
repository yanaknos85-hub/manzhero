package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(title = "Информация о согласии на обработку персональных данных", description = "Данные о согласии на обработку ПДН")
@Builder(toBuilder = true)
public record AgreementPdnDTO(
    @Schema(description = "Идентификатор")
    UUID id,

    @Schema(description = "Идентификатор сотрудника")
    UUID employeeId,

    @Schema(description = "Идентификатор транспортного средства")
    UUID carId,

    @Schema(description = "Имя файла")
    String fileName,

    @Schema(description = "Размер файла")
    Integer fileSize,

    @Schema(description = "Формат файла")
    String fileFormat) {}