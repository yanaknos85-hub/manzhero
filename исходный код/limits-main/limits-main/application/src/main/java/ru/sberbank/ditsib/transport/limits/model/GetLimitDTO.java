package ru.sberbank.ditsib.transport.limits.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
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
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Limit ID formed by Limit-ID rules
     */
    @NotNull
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;

    /**
     * Limit owner
     */
    @Schema(description = "Владелец лимита")
    private UUID limitOwner;

    /**
     * Status of limit
     */
    @NotNull
    @Schema(description = "Статус лимита")
    private LimitStatus limitStatus;

    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год")
    private Integer year;

    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Тип распределения")
    private LimitSharingType limitSharingType;

    /**
     * Limit service type
     */
    @NotNull
    @Schema(description = "Услуга")
    private String limitServiceType;

    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;

    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Резерв")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal reserve;

    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Экономия")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal economy;

    /**
     * Date and time or request creation
     */
    @NotNull
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;

    /**
     * Final sharing flag
     */
    @Schema(description = "Конечное распределение")
    private Boolean finalSharing;

    /**
     * Use my limit flag
     */
    @Schema(description = "Использовать лимит моего подразделения")
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
    @Builder.Default
    List<GetLimitSharingDTO> limitSharingDTOList = List.of();

    /**
     * Владелец лимита.
     */
    @Schema(description = "Владелец лимита")
    GetEmployeeDTO owner;
}
