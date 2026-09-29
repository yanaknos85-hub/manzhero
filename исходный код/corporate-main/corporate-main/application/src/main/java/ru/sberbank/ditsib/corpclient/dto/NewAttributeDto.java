package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@Schema(title = "Информация о новом признаке", description = "Новый признак сорудника")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewAttributeDto {

    /**
     * Name of attribute
     */
    @NotBlank
    @Schema(description = "Название", maxLength = 128)
    @Size(max = 128)
    private String name;

    /**
     * Status of department.
     */
    @Schema(description = "Статус", maxLength = 128)
    private AttributeStatus status;
}
