package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EmployeeDocumentFileDTO(
    @Schema(description = "Имя файла")
    String fileName,

    @Schema(description = "Размер файла")
    Integer fileSize,

    @Schema(description = "Формат файла")
    String fileFormat
) {}
