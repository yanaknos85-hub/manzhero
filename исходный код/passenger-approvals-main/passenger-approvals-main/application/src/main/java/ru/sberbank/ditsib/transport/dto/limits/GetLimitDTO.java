package ru.sberbank.ditsib.transport.dto.limits;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Лимит (Чтение)", description = "Данные лимита для чтения")
@Builder
public class GetLimitDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Limit ID formed by Limit-ID rules
     */
    @NotNull
    @Schema(description = "Человекочитаемый идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    /**
     * Limit owner
     */
    @NotNull
    @Schema(description = "Владелец лимита", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID limitOwner;
    
    /**
     * Status of limit
     */
    @NotNull
    @Schema(description = "Статус лимита", requiredMode = Schema.RequiredMode.REQUIRED)
    private LimitStatus limitStatus;
    
    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Тип распределения", requiredMode = Schema.RequiredMode.REQUIRED)
    private LimitSharingType limitSharingType;
    
    /**
     * Limit service type
     */
    @NotNull
    @Schema(description = "Услуга", requiredMode = Schema.RequiredMode.REQUIRED)
    private LimitServiceType limitServiceType;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Сумма в копейках", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sum;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Резерв", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reserve;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Экономия", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long economy;
    
    /**
     * Date and time or request creation
     */
    @NotNull
    @Schema(description = "Время создания", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime creationTime;
    
    /**
     * Final sharing flag
     */
    @Schema(description = "Конечное распределение", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean finalSharing;
    
    /**
     * Use my limit flag
     */
    @Schema(description = "Использовать лимит моего подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean useThisLimit;
    
    /**
     * Limit type
     */
    @Schema(description = "Тип")
    private LimitType limitType;
    
    /**
     * Department
     */
    @Schema(description = "Подразделение")
    private GetDepartmentDTO department;
    
    /**
     * Employee
     */
    @Schema(description = "Сотрудник")
    private GetEmployeeDTO employee;
    
    /**
     * Parent limit
     */
    @Schema(description = "Родительский лимит")
    private UUID parentLimitId;
    
    /**
     * Parent limit
     */
    @Schema(description = "Распределения лимита по годичные")
    List<GetLimitSharingDTO> limitSharingDTOList;
}


