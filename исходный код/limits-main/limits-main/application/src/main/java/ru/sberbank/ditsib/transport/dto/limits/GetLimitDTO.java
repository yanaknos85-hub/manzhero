package ru.sberbank.ditsib.transport.dto.limits;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.model.GetDepartmentDTO;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(
    title = "Лимит (Чтение)",
    description = "Данные лимита для чтения"
)
@Builder
@Getter
@Setter
public class GetLimitDTO {

    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;

    @NotNull
    @Schema(description = "Владелец лимита")
    private UUID limitOwner;

    @NotNull
    @Schema(description = "Статус лимита")
    private LimitStatus limitStatus;

    @NotNull
    @Schema(description = "Год" )
    private Integer year;

    @NotNull
    @Schema(description = "Тип распределения")
    private LimitSharingType limitSharingType;

    @NotNull
    @Schema(description = "Услуга")
    private LimitServiceType limitServiceType;

    @Min(0L)
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;

    @Min(0L)
    @NotNull
    @Schema(description = "Резерв")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal reserve;

    @Min(0L)
    @NotNull
    @Schema(description = "Экономия")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal economy;

    @NotNull
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;

    @Schema(description = "Конечное распределение")
    private Boolean finalSharing;

    @Schema(description = "Использовать лимит моего подразделения")
    private Boolean useThisLimit;

    @Schema(description = "Тип")
    private LimitType limitType;

    @Schema(description = "Подразделение")
    private GetDepartmentDTO department;

    @Schema(description = "Сотрудник")
    private GetEmployeeDTO employee;

    @Schema(description = "Родительский лимит")
    private UUID parentLimitId;

    @Schema(description = "Распределения лимита по годичные")
    List<GetLimitSharingDTO> limitSharingDTOList;
}
