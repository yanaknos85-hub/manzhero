package ru.sberbank.ditsib.corpclient.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект обмена данными об организациях.
 */
@Getter
@Setter
@ToString
public class NewOrganizationFileDTO {
    
    /**
     * Юр. название организации.
     */
    @NotBlank
    private String officialName;
    
    /**
     * Юр. адрес.
     */
    @NotBlank
    private String address;
    
    /**
     * Список контактов.
     */
    private String phones;
    
    /**
     * Список E-Mail.
     */
    private String email;
    
    /**
     * Сайт.
     */
    private String sites;
    
    /**
     * ОГРН.
     */
    private String msrn;
    
    /**
     * ИНН.
     */
    private String tin;

    /**
     * Код организационной единицы.
     */
    private Integer organizationCode;
}
