package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Setter
@Getter
@Schema(title = "Информация о признаке", description = "Признак сорудника")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDto extends NewAttributeDto {
    /**
     * ID.
     */
    @NonNull
    @Schema(description = "Идентификатор")
    private UUID id;

}
