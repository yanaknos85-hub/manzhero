package ru.sberbank.ditsib.transport.limits.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Object with data about action.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Статус резервирования для массива", description = "Статус резервирования для массива")
@Builder
public class MassLimitReservationResultDto {

    /**
     * List of reservation results
     */
    @Schema(description = "Результаты резервирования")
    private List<LimitReservationResultDto> reservationResults;
    
    /**
     * Reservation status.
     */
    @Schema(description = "Общий статус резервирования")
    private LimitReservationStatus limitReservationStatus;

    /**
     * Message.
     */
    @Schema(description = "Общее сообщение")
    private String message;

}
