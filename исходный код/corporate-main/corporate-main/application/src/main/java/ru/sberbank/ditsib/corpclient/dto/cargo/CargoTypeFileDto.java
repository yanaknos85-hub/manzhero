package ru.sberbank.ditsib.corpclient.dto.cargo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data object for files.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargoTypeFileDto {
    
    /**
     * Название груза
     */
    @NotNull
    private String name;
    
    /**
     * Вид груза
     */
    @NotNull
    private String type;
    
    /**
     * Категория груза
     */
    @NotNull
    private String category;
    
    /**
     * Длина груза
     */
    private double length;
    
    /**
     * Ширина груза
     */
    private double width;
    
    /**
     * Высота груза
     */
    private double height;
    
    /**
     * Вес груза
     */
    private double weight;
    
    /**
     * Объем
     */
    private Double volume;

    /**
     * Организация
     */
    private UUID organizationId;

}
