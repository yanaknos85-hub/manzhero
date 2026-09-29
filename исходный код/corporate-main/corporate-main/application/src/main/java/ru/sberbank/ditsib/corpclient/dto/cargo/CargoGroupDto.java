package ru.sberbank.ditsib.corpclient.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@Schema(title = "Группа груза", description = "Данные группы груза")
@SuperBuilder()
@NoArgsConstructor
@AllArgsConstructor
public class CargoGroupDto {
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Название груза
     */
    @NotBlank(message = "Поле название оьязательно для заполнения")
    @Schema(description = "Название")
    private String name;

    @Schema(description = "Количество единиц груза")
    private int count;

    @Schema(description = "Вид груза")
    private UUID cargoTypeId;
}
