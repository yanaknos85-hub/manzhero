package ru.sberbank.ditsib.corpclient.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Entity of cargo type.
 */
@Data
@Schema(title = "Вид груза", description = "Данные вида груза")
@SuperBuilder()
@NoArgsConstructor
@AllArgsConstructor
public class CargoTypeDto {
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

    /**
     * Вид груза
     */
    @Schema(description = "Вид")
    private CargoTypeEnum type;

    /**
     * Категория груза
     */
    @Schema(description = "Категория")
    private CargoCategoryEnum category;

    /**
     * Длина груза
     */
    @Schema(description = "Длина")
    private double length;

    /**
     * Ширина груза
     */
    @Schema(description = "Ширина")
    private double width;

    /**
     * Высота груза
     */
    @Schema(description = "Высота")
    private double height;

    /**
     * Вес груза
     */
    @Schema(description = "Вес")
    private double weight;

    /**
     * Объем груза
     */
    @Schema(description = "Объем")
    private double volume;

    /**
     * Активность
     */
    @Schema(description = "Активность")
    @Builder.Default
    private boolean active = true;

    @Schema(description = "Id организации")
    private UUID organizationId;

    @Schema(description = "Универсальная организация")
    @Builder.Default
    private boolean isUniversal = false;

}
