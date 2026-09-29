package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Информация о полисе ОСАГО", description = "Данные ОСАГО")
@Builder(toBuilder = true)
public record OsagoDTO(
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
        String fileFormat,

        @Schema(description = "Серия документа")
        String seria,

        @Schema(description = "Номер документа")
        String number,

        @Schema(description = "Время начала действия документа")
        LocalDateTime startTimeDocument,

        @Schema(description = "Время окончания действия документа")
        LocalDateTime finalTimeDocument,

        @Schema(description = "Государственный регистрационный знак транспортного средства")
        String registrationNumber
) {}
