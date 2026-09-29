package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Движение денежных средств по лимитами",
        description = "Движение денежных средств по лимитами")
public class GetLimitHistoryDTO {

    @Schema(description = "Уникальный идентификатор записи")
    private UUID id;
    
    /**
     * Source limit id
     */
    @Schema(description = "Идентификатор лимита")
    private UUID limitId;
    
    /**
     * Source transport type
     */
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * Author of sharing
     */
    @Schema(description = "Автор")
    private UUID authorId;
    
    /**
     * Organization id.
     */
    @Schema(description = "Организация")
    private UUID organizationId;
    
    /**
     * Date and time or record creation.
     */
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;
    
    /**
     * Year
     */
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Исходный период
     */
    @Schema(description = "Период")
    private Integer period;
    
    /**
     * Sum
     */
    @Schema(description = "Сумма")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Counterpart limit id
     */
    @Schema(description = "Идентификатор второго лимита")
    private UUID counterpartLimitId;
    
    /**
     * Target transport type
     */
    @Schema(description = "Тип транспорта второго лимита")
    private TransportTypeEnum counterpartTransportType;
    
    /**
     * Целевой период
     */
    @Schema(description = "Период второго лимита")
    private Integer counterpartPeriod;
    
    /**
     * Year
     */
    @Schema(description = "Тип записи")
    private LimitHistoryType historyType;
    
    /**
     * Limit sharing type
     */
    @Schema(description = "Тип услуги")
    private String limitServiceType;
    
    /**
     * Limit sharing type
     */
    @Schema(description = "Сумма распределения")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitSharingSum;
    
    /**
     * Limit sharing type
     */
    @Schema(description = "Баланс распределения")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitSharingBalance;
    
    /**
     * Limit sharing type
     */
    @Schema(description = "Сумма распределения за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitSharingPerperiodSum;
    
    /**
     * Limit sharing type
     */
    @Schema(description = "Баланс распределения за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal limitSharingPerperiodBalance;
}
