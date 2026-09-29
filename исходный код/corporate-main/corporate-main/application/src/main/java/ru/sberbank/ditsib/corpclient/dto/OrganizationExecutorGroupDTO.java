package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object with data about organization.
 */
@Getter
@Setter
@Schema(title = "Данные об организации для групп исполнителей", description = "Данные организации")
public class OrganizationExecutorGroupDTO extends OrganizationShortDTO {

    /**
     * Status.
     */
    @Schema(description = "Статус")
    private OrganizationStatus status;
}
