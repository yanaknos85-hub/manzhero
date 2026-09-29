package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * New delegate record  DTO
 */
@Setter
@Getter
@JsonPropertyOrder({ "id" })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Информация о новой записи о делегате и периоде делегирования", description = "Данные делегата")
public class DelegateRecordDTO {
    
    /**
     * Supervisor id.
     */
    @NotNull
    @Schema(description = "Идентификатор руководителя")
    UUID supervisorId;
    
    /**
     * Delegate id.
     */
    @NotNull
    @Schema(description = "Идентификатор делегата")
    UUID delegateId;
    
    
    /**
     * Start of delegating period
     */
    @NotNull
    @Schema(description = "Начало  периода делегирования")
    LocalDate startDate;
    
    /**
     * End of delegating period
     */
    @NotNull
    @Schema(description = "Окончание периода делегирования")
    LocalDate endDate;
    
    /**
     * Transport type id.
     */
    @NotNull
    @Schema(description = "Тип транспорта")
    TransportTypeEnum transportType;
}
