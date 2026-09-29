package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.UUID;

@Jacksonized
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "День недели у цели поездки", description = "Фильтры цели поездки")
public class TripPurposeWeekdayDTO {
    @JsonIgnore
    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "День недели у цели поездки")
    private DayOfWeek weekday;
}
