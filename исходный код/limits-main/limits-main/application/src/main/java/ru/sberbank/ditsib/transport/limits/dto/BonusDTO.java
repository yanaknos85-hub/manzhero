package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Бонусный счёт", description = "Данные бонусного счёта")
@Builder
public class BonusDTO {
    
    @Schema(description = "ID владельца бонусного счёта")
    private UUID ownerId;
    
    @Schema(description = "Сумма бонусного счёта в копейках (хардкод 2000 рублей)")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    @Schema(description = "Баланс (сумма - зарезервированные средства) в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balance;
    
    @Schema(description = "Список операций со счётом")
    private List<BonusRequestDTO> requests;
    
}
