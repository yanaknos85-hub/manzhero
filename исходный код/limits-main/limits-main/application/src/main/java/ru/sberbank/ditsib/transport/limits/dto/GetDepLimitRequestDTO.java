package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки")
@Builder
public class GetDepLimitRequestDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Human readable id
     */
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * Id of limit used to cover trip cost
     */
    @Schema(description = "Идентификатор пользователя")
    private UUID authorId;
    
    /**
     * Year
     */
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Month
     */
    @Schema(description = "Порядковый номер периода")
    private Integer period;
    
    /**
     * ID of transport type.
     */
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Comment
     */
    @Schema(description = "Комментарий")
    private String description;
    
    /**
     * Причина отказа
     */
    @Schema(description = "Причина откза")
    private String declineReason;
    
    /**
     * Запросить у
     */
    @Schema(description = "Запросить у")
    private String askTargets;
    
    /**
     * Смежные подразделения
     */
    @Schema(description = "Смежные подразделения")
    private final Set<UUID> departments = new HashSet<>();
    
    /**
     * Status of approval
     */
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
    
    /**
     * Status of fill request
     */
    @Schema(description = "Статус заявки")
    private LimitRequestStatus status;
    
    @Schema(description = "Дата создания")
    private LocalDateTime creationTime;
}
