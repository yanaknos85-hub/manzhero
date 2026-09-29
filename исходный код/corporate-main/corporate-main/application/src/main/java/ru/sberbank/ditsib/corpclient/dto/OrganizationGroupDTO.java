package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Данные группы организаций")
public record OrganizationGroupDTO(

        @NotNull
        @Schema(description = "Наименование группы организаций")
        String name,

        @NotEmpty
        @Schema(description = "Список ID организаций, входящи в группу")
        List<UUID> organizationIds,

        @Schema(description = "Принадлежность к внутренней группе компаний")
        boolean internal
) {
}
