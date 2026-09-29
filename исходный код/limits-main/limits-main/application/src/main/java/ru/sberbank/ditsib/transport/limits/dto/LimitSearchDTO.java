package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;

import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Лимит", description = "Данные лимита")
public class LimitSearchDTO {
    
    /**
     * Department
     */
    @Schema(description = "Идентификатор организации")
    private UUID organizationId;

    /**
     * Department
     */
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;

    /**
     * Department
     */
    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;

    /**
     * Department
     */
    @Schema(description = "Идентификатор сотрудника")
    private UUID employeeId;

    /**
     * Department
     */
    @Schema(description = "Распределяющее подразделение")
    private String parentDepartment;
    
    /**
     * Employee
     */
    @Schema(description = "Владелец лимита")
    private String limitOwner;
    
    /**
     * Limit ID formed by Limit-ID rules
     */
    private UUID limitId;

    /**
     * Limit ID formed by Limit-ID rules
     */
    private UUID parentLimitId;
    
    /**
     * Limit ID formed by Limit-ID rules
     */
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableLimitId;
    
    /**
     * Year
     */
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Status
     */
    @Schema(description = "Статус")
    private LimitStatus limitStatus;
    
    /**
     * Limit service type
     */
    @Schema(description = "Услуга")
    private String limitServiceType;
    
    /**
     * Limit type
     */
    @Schema(description = "Тип")
    private LimitType limitType;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private Direction direction = Direction.ASC;
    
    /**
     * Active
     *
     * @deprecated Вывести из эксплуатации!
     */
    @Deprecated(forRemoval = true)
    @Schema(description = "Признак активности")
    @Builder.Default
    private boolean active = true;
    
}
