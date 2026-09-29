package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Data transfer object with data about existing department.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id", "name", "filialFlag", "children" })
@Schema(title = "Информация о подразделении", description = "Данные подразделения")
public class DepartmentTreeNodeDTO extends DepartmentSelectDTO {

    @Schema(description = "Идентификатор")
    UUID id;

    @Schema(description = "Название")
    String name;

    @Schema(description = "Флаг филиала")
    boolean filialFlag;
    
    /**
     * Children collection
     */
    @Schema(description = "Дочерние подразделения")
    private Set<UUID> children = new HashSet<>();
}
