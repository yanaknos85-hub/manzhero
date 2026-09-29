package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Данные созданной группы организаций")
public record OrganizationGroupResponseDTO(

        @Schema(description = "ID группы организаций")
        UUID id,

        @Schema(description = "Наименование созданной группы организаций")
        String name,

        @Schema(description = "Принадлежность к внутренней группе компаний")
        boolean internal
) {
}
