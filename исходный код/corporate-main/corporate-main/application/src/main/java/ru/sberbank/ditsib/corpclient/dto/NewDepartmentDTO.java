package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Data transfer object with data about new department.
 */
@Getter
@Setter
@ToString
@Schema(title = "Информация о подразделении", description = "Данные подразделения")
public class NewDepartmentDTO {
    
    @Schema(description = "Идентификатор геозоны")
    private UUID geozoneId;
    
    /**
     * Unique code
     */
    @NotNull
    @Size(min = 1, max = 10)
    @Schema(description = "Код подразделения", minLength = 1, maxLength = 10)
    private String code;
    
    /**
     * Name of department
     */
    @NotNull
    @Size(min = 1, max = 255)
    @Schema(description = "Название", minLength = 1, maxLength = 255)
    @JsonProperty("departmentName")
    private String name;
    
    /**
     * Parent department
     */
    @Schema(description = "Родителькое подразделение")
    private DepartmentParentDTO parent;
    
    /**
     * Head of department
     */
    @Schema(description = "Глава подразделения")
    private DepartmentHeadDTO departmentHead;
    
    /**
     * location
     */
    @Size(max = 30)
    @Schema(description = "Местоположение", maxLength = 30)
    private String location;
    
    /**
     * Start date of the actual recording of the department
     */
    @Schema(description = "Дата начала записи")
    private LocalDate startRecordingDate;
    
    /**
     * End date of the actual recording of the department (liquidation)
     */
    @Schema(description = "Дата окончания записи")
    private LocalDate endRecordingDate;
    
    /**
     * Organization level code
     */
    @Schema(description = "Код уровня организации")
    private int levelCode;
    
    /**
     * Organization level name
     */
    @Schema(description = "Наименование уровня подразделения")
    private String levelName;
    
    /**
     * Status of department.
     */
    @Schema(description = "Статус", defaultValue = "ACTIVE")
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
    
    /**
     * Easup id.
     */
    @Schema(description = "Идентификатор ЕАСУП, для внутрибанковских структур поле обязательное")
    private String easupId;

    /**
     * Признак ручного изменения руководителя.
     */
    @Schema(description = "Признак ручного изменения руководителя")
    private boolean isHandmade;

    /**
     * Признак филиала.
     */
    @Schema(description = "Признак филиала")
    private Boolean filialFlag;

}
