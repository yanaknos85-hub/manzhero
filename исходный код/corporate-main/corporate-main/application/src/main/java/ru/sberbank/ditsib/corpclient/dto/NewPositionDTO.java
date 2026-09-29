package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Data transfer object with data about position.
 */
@Getter
@Setter
@ToString
@Schema(title = "Новые данные о подразделения", description = "Данные подразделения")
public class NewPositionDTO {
    
    /**
     * Organization
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID organizationId;
    
    /**
     * Position
     */
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название", maxLength = 255)
    @JsonProperty("positionName")
    private String name;
    
    /**
     * Available classes
     */
    @Schema(description = "Доступные классы")
    private Set<TaxiClass> availableClasses = new HashSet<>();
    
    /**
     * Self reques approvement trait
     */
    @Schema(description = "Самосогласование")
    private boolean selfApproved;
}
