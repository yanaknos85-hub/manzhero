package ru.sberbank.ditsib.transport.approvals.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Согласование заявки или поездки", description = "Данные заявки или поездки")
@Builder
public class TripApproveDTO {
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Passenger
     */
    @NotNull
    @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO passenger;
    
    /**
     * Request id
     */
    @NotNull
    @Schema(description = "Индентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID requestId;
    
    /**
     * Type of transport used for request
     */
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transportType;
    
    /**
     * Type of public compensation used for request
     */
    @Schema(description = "Тип компенсации")
    private PublicCompensationType publicCompensationType;
    
    /**
     * Class of taxi used for request
     */
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    /**
     * Desired date and time of trip
     */
    @Schema(description = "Дата создания", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime creationTime;
    
    /**
     * Desired date and time of trip
     */
    @Schema(description = "Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime desiredDate;
    
    /**
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Идентификатор цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID purposeId;
    
    /**
     * Trip purpose label
     */
    @NotNull
    @Schema(description = "Описание цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purposeLabel;
    
    /**
     * cost
     */
    @Min(0)
    @Schema(description = "Стоимость поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private double cost;
    
    /**
     * Limit
     */
    @Schema(description = "Остаток по используемому лимиту (без учета всех заявок, в статусе \"На согласовании\") для" +
                          " выбранного вида транспорта")
    private Long restOfLimit;
    
    @Schema(description = "Полная сумма лимита подразделения")
    private Long sumLimit;
    
    @Schema(description = "Статус согласования", requiredMode = Schema.RequiredMode.REQUIRED)
    private ApprovalStatus status;
    
    @Schema(description = "Путевые точки предполагаемой поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, Object>> waypoints = new ArrayList<>();
    
    @Schema(description = "Предполагаемое время в пути", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = DurationMillisConverter.class)
    private Duration expectedTime;
    
    @Schema(description = "Предполагаемое расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
    private double expectedDistance;
    
    @Schema(description = "Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED)
    private int passengerCount;
    
    @Schema(description = "Идентификатор поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestHumanReadableId;
    
    @Schema(description = "Причина отклонения заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
    
    @Schema(description = "Признак совместной поездки")
    private boolean isCoopTrip;
    
    @Schema(description = "Тип согласования")
    private Type type;
    
    @Schema(description = "Таймзона")
    private String timeZone;

    @Schema(description = "Сообщения о фроде")
    List<FraudCommentDTO> fraudComment = new ArrayList<>();
}
