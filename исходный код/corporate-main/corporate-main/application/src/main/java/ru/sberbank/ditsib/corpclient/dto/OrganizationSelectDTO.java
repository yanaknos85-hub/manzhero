package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Data transfer object with data about organization.
 */
@Setter
@Getter
@JsonPropertyOrder({ "id" })
@Schema(title = "Данные об организации", description = "Данные организации")
public class OrganizationSelectDTO extends NewOrganizationDTO {
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор (цифровой)")
    private Long digitId;
    
    /**
     * Status.
     */
    @Schema(description = "Статус")
    private OrganizationStatus status;

    /**
     * OrganizationCode.
     */
    @Schema(description = "Код организационной единицы")
    private Integer organizationCode;
}
