package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data of approver.
 */
@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки")
@Builder
public class ApproverDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Id of limit used to cover trip cost
     */
    @Schema(description = "Идентификатор пользователя")
    private UUID departmentId;
    
    /**
     * Id of limit used to cover trip cost
     */
    @Schema(description = "Идентификатор пользователя")
    private UUID employeeId;
    
    /**
     * Id of limit used to cover trip cost
     */
    @Schema(description = "Идентификатор пользователя")
    private String employeeFio;
    
    /**
     * Id of limit used to cover trip cost
     */
    @Schema(description = "Идентификатор пользователя")
    private UUID limitRequestId;
    
    /**
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Status of approval
     */
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
}
