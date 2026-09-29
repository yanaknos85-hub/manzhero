package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Информация о водительском удостоверении", description = "Данные водительского удостоверения")
@Builder(toBuilder = true)
public record DriverLicDTO(

    @Schema(description = "Идентификатор")
    UUID id,

    @Schema(description = "Идентификатор сотрудника")
    UUID employeeId,

    @Schema(description = "Имя файла")
    String fileName,

    @Schema(description = "Размер файла")
    Integer fileSize,

    @Schema(description = "Формат файла")
    String fileFormat,

    @Schema(description = "Серия документа")
    String seria,

    @Schema(description = "Номер документа")
    String number,

    @Schema(description = "Дата выдачи документа")
    LocalDateTime issueDateDocument,

    @Schema(description = "Кем выдан")
    String issue,

    @Schema(description = "Где выдан")
    String placeIssue,

    @Schema(description = "Время окончания действия документа")
    LocalDateTime finalTimeDocument,

    @Schema(description = "Категории в водительском удостоверении")
    String categoria) {
}
