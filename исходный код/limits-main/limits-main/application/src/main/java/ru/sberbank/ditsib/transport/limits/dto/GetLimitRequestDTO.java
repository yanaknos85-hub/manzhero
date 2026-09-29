package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки")
@Builder
public class GetLimitRequestDTO {
    
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
    private GetEmployeeDTO author;
    
    /**
     * Year.
     */
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Period.
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
     * Status of request
     */
    @Schema(description = "Статус заявки")
    private LimitRequestStatus status;
    
    /**
     * Status code
     */
    @Schema(description = "Код статуса")
    private Integer statusCode;
    
    /**
     * Creation time
     */
    @Schema(description = "Дата создания")
    private LocalDateTime creationTime;
    
    /**
     * Type of limit for request
     */
    @Schema(description = "Тип лимита на который подана заявка")
    private LimitType limitType;
    
    /**
     * Запросить у
     */
    @Schema(description = "Запросить у")
    private LimitRequestAskTargets askTargets;
    
    /**
     * Таблица одобрений
     */
    @Schema(description = "Таблица одобрений")
    List<ApproverDTO> approverDtoList;
    
    /**
     * ID лимита
     */
    @Schema(description = "ID лимита")
    private UUID limitId;
    
    /**
     * Человекочитаемый айди лимита
     */
    @Schema(description = "Человекочитаемый айди лимита")
    private String limitHumanreadableid;
    
    /**
     * Тип распределения лимита
     */
    @Schema(description = "Тип распределения лимита")
    private LimitSharingType limitSharingType;
    
    /**
     * Плановое значение
     */
    @Schema(description = "Плановое значение")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal plannedSum;
    
    /**
     * Сумма лимита
     */
    @Schema(description = "Сумма лимита")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitSum;
    
    /**
     * Остаток по лимиту
     */
    @Schema(description = "Остаток по лимиту")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitBalance;
    
    //--------------------------------------
    
    /**
     * ID лимита
     */
    @Schema(description = "ID лимита")
    private UUID parentLimitId;
    
    /**
     * Человекочитаемый айди лимита
     */
    @Schema(description = "Человекочитаемый айди лимита")
    private String parentLimitHumanreadableid;
    
    /**
     * Сумма лимита
     */
    @Schema(description = "Сумма лимита")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal parentLimitSum;
    
    /**
     * Остаток по лимиту
     */
    @Schema(description = "Остаток по лимиту")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal parentLimitBalance;
}
