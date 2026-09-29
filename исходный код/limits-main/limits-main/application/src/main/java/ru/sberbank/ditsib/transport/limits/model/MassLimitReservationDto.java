package ru.sberbank.ditsib.transport.limits.model;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static ru.sberbank.ditsib.transport.limits.model.ReservationStrategy.ALL;

/**
 * Object with data about action.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MassLimitReservationDto {
    
    /**
     * List of limit reservation data
     */
    @NotNull
    @Builder.Default
    List<LimitReservationDto> limitReservationDtos = new ArrayList<>();
    
    /**
     * Limit reservation strategy (ALL, ANY, MANUAL)
     */
    @NotNull
    @Builder.Default
    private ReservationStrategy reservationStrategy = ALL;
    
    
}
