package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO Настройки согласований заявок на такси
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({ "organizationId"})
@Schema(title = "Настройки согласований заявок на такси")
public class TaxiApprovalsSettingsDTO extends ApprovalsSettingsDTO {
}
