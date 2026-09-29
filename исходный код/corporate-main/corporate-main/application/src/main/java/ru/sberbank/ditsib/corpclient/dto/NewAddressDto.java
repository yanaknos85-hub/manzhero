package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity of address.
 */
@Setter
@Getter
@Schema(title = "Новые данные об адресе", description = "Данные адреса")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewAddressDto {
    
    /**
     * Label.
     */
    @NotBlank
    @Schema(description = "Метка", maxLength = 255)
    private String label;
    
    /**
     * Country.
     */
    @NotBlank
    @Schema(description = "Страна", maxLength = 255)
    @Length(max = 255)
    private String country;
    
    /**
     * Region.
     */
    @NotBlank
    @Schema(description = "Регион", maxLength = 255)
    @Length(max = 255)
    private String region;
    
    /**
     * City.
     */
    @NotBlank
    @Schema(description = "Город", maxLength = 255)
    @Length(max = 255)
    private String city;
    
    /**
     * Street.
     */
    @NotBlank
    @Schema(description = "Улица", maxLength = 255)
    @Length(max = 255)
    private String street;
    
    /**
     * House.
     */
    @Schema(description = "Дом", maxLength = 255)
    @Length(max = 255)
    private String house;
    
    /**
     * Building.
     */
    @Schema(description = "Строение", maxLength = 255)
    @Length(max = 255)
    private String building;
    
    /**
     * Structure.
     */
    @Schema(description = "Корпус", maxLength = 255)
    @Length(max = 255)
    private String structure;
    
    /**
     * Structure.
     */
    @NotNull
    @Schema(description = "Широта")
    private Double latitude;
    
    /**
     * Structure.
     */
    @NotNull
    @Schema(description = "Долгота")
    private Double longitude;
    
}
