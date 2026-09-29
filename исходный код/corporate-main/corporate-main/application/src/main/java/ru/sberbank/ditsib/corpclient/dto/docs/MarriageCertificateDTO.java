package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Информация о свидетельстве о браке", description = "Данные свидетельства о браке")
@Builder(toBuilder = true)
public record MarriageCertificateDTO(
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
        LocalDateTime issueDateDocument
) {
}
