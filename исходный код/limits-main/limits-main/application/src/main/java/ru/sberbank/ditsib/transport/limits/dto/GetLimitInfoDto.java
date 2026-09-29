package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;

@Schema(
        title = "Лимит",
        description = " Информация о доступном лимите"
)
@Data
@Builder
public class GetLimitInfoDto {
    
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    @Schema(description = "Год")
    private int year;
    
    @Schema(description = "Тип распределения")
    private LimitSharingType limitSharingType;
    
    @Schema(description = "Месяц распределения")
    private int month;
    
    @Schema(description = "Квартал распределения")
    private int quarter;
    
    @Schema(description = "Общий лимит")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    @Schema(description = "Доступный лимит")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balance;
    
}
