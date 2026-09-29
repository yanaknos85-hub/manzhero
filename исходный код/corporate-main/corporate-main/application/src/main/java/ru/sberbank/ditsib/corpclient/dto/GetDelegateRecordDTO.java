package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;

import java.util.UUID;

/**
 * New delegate record  DTO
 */
@Setter
@Getter
@JsonPropertyOrder({ "id" })
@SuperBuilder
@NoArgsConstructor
@Schema(title = "Информация о cуществующей записи о делегате и периоде делегирования", description = "Данные делегата")
public class GetDelegateRecordDTO extends DelegateRecordDTO {
    
    /**
     * Identifier
     */
    @Schema(description = "Сотрудник делегат")
    private EmployeeDTO delegateEmployee;
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Status
     */
    private RecordStatus status;
}
