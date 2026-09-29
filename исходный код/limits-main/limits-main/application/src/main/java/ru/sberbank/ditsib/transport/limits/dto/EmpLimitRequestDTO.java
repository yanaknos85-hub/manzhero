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

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Новая заявка", description = "Данные новой заявки")
public class EmpLimitRequestDTO {
    
    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Month
     */
    @NotNull
    @Schema(description = "Порядковый номер периода (0-based)")
    private Integer period;
    
    /**
     * ID of transport type.
     */
    @NotNull
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
}
