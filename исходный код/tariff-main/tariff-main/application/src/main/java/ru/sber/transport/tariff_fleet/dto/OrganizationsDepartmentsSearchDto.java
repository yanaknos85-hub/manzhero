package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(title = "Поиск департаментов", description = "Поиск департаментов организаций по идентификатору организаций")
public record OrganizationsDepartmentsSearchDto(
        
        @NotEmpty
        @Schema(title = "Идентификаторы организаций",
                description = "Список идентификаторов организаций",
                example = "[\"123e4567-e89b-12d3-a456-426655440000\", \"1341f681-892a-46b0-ada9-764193f34ad9\"]")
        List<UUID> organizationId) {
}