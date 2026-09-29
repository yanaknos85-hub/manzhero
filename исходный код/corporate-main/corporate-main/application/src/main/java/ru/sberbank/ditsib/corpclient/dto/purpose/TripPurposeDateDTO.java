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
@Schema(title = "Дата цели поездки", description = "Фильтры цели поездки. Требуется переработка, сейчас дата начала и окончания произвольные для поддержки перехода через день")
public class TripPurposeDateDTO {

    @JsonIgnore
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @NotNull
    @Schema(description = "Дата начала фильтра")
    private LocalDateTime startDate;
    
    @NotNull
    @Schema(description = "Дата конца фильтра")
    private LocalDateTime endDate;

}
