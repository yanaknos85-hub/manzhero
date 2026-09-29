package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusOperation;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Запрос на изменение бонусного счёта", description = "Запрос на изменение бонусного счёта")
@Builder
public class BonusRequestDTO {
    
    @Schema(description = "id")
    private UUID id;
    
    @Schema(description = "Сумма операции")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    @Schema(description = "Операция с бонусным счётом")
    private BonusOperation operation;
    
    @Schema(description = "Статус операции с бонусным счётом")
    private BonusRequestStatus status;
    
    @Schema(description = "Время последнего обновления операции")
    private LocalDateTime updateTime;
    
    @Schema(description = "Причина снятия/пополнения бонусного счёта")
    private String reason;
}
