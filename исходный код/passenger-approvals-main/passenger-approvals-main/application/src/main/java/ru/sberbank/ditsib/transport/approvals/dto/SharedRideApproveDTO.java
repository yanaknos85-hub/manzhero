package ru.sberbank.ditsib.transport.approvals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(title = "Согласование заявки на присоединение к совместной поездке",
        description = "Данные согласования заявки на присоединение к совместной поездке")
public class SharedRideApproveDTO extends TripApproveDTO {
    /**
     * Request id
     */
    @NotNull
    @Schema(description = "Индентификатор присоединямой заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID addRequestId;
    
}
