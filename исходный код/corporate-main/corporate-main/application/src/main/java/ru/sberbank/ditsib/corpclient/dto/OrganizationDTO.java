package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object with data about organization.
 */
@Setter
@Getter
@JsonPropertyOrder({ "id" })
@Schema(title = "Данные об организации", description = "Данные организации")
public class OrganizationDTO extends OrganizationSelectDTO {
}
