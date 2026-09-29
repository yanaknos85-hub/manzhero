package ru.sberbank.ditsib.corpclient.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@Schema(title = "Группа и Вид груза", description = "Данные группы и вида груза")
@SuperBuilder()
@NoArgsConstructor
@AllArgsConstructor
public class CagroGroupTypeDto extends CargoTypeDto {

    @Schema(description = "Количество единиц груза")
    private int count;

}
