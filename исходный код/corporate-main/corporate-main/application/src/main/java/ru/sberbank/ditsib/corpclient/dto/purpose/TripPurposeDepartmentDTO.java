package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.sberbank.ditsib.corpclient.dto.DepartmentShortDTO;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Schema(title = "Депертамент цели поездки", description = "Фильтры цели поездки")
public class TripPurposeDepartmentDTO {
    @JsonIgnore
    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "Депертамент цели поездки")
    private DepartmentShortDTO department;
}
