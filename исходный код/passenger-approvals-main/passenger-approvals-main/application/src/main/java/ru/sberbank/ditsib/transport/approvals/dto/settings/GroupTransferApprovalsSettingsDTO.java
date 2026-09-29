package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO Настройки согласований заявок на групповой трансфер
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({ "organizationId" })
@Schema(title = "Настройки согласований заявок на групповой трансфер")
public class GroupTransferApprovalsSettingsDTO extends ApprovalsSettingsDTO {
}
