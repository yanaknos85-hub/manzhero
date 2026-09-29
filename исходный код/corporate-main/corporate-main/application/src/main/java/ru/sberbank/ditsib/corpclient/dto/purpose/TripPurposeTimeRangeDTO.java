package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Jacksonized
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Интервал времени у цели поездки", description = "Фильтры цели поездки. Требуется переработка, сейчас время начала и окончания произвольные для поддержки перехода через день")
public class TripPurposeTimeRangeDTO {

    @JsonIgnore
    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "Начало интервала времени у цели поездки")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "Конец интервала времени у цели поездки")
    private LocalDateTime endTime;

}
