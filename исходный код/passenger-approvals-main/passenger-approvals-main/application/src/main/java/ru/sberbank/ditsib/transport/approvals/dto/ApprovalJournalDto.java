package ru.sberbank.ditsib.transport.approvals.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.With;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@With
@Schema(title = "Согласование заявки или поездки", description = "Данные заявки или поездки")
public record ApprovalJournalDto(
        @NotNull
        @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @NotNull
        @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
        EmployeeDTO passenger,
        @NotNull
        @Schema(description = "Индентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID requestId,
        @NotNull
        @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        String transportType,
        @Nullable
        @Schema(description = "Класс такси")
        TaxiClass taxiClass,
        @NotNull
        @Schema(description = "Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime desiredDate,
        @NotNull
        @Schema(description = "Идентификатор цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID purposeId,
        @NotNull
        @Schema(description = "Описание цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        String purposeLabel,
        @NotNull
        @Min(0)
        @Schema(description = "Стоимость поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        double cost,
        @NotNull
        @Schema(description = "Статус согласования", requiredMode = Schema.RequiredMode.REQUIRED)
        String status,
        @NotNull
        @Schema(description = "Путевые точки предполагаемой поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Map<String, Object>> waypoints,
        @NotNull
        @JsonSerialize(using = DurationMillisConverter.class)
        @Schema(description = "Предполагаемое время в пути", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration expectedTime,
        @NotNull
        @Schema(description = "Предполагаемое расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
        double expectedDistance,
        @NotNull
        @Schema(description = "Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED)
        int passengerCount,
        @NotNull
        @Schema(description = "Идентификатор поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        String requestHumanReadableId,
        @Schema(description = "Признак совместной поездки")
        boolean isCoopTrip,
        @Nullable
        @Schema(description = "Тип согласования")
        String type,
        @Nullable
        @Schema(description = "Таймзона")
        String timeZone,
        @NotNull
        @Schema(description = "Индентификатор присоединямой заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID addRequestId,
        @Nullable
        @Schema(description = "Сообщения о фроде")
        List<FraudCommentDTO> fraudComment
) {
}