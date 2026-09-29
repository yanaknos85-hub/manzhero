package ru.sberbank.ditsib.corpclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект подразделения из файла.
 */
@Getter
@Setter
@NoArgsConstructor
public class DepartmentFileDTO {
    
    /**
     * Организация.
     */
    @NotBlank
    private String organization;
    
    /**
     * Название подразделения.
     */
    private String name;
    
    /**
     * Название уровня.
     */
    private String level;
    
    /**
     * Код подразделения.
     */
    @NotBlank
    private String code;
    
    /**
     * Код типа уровня.
     */
    private Integer levelCode;
    
    /**
     * Местоположение.
     */
    private String location;
    
    /**
     * Код родительского подразделения.
     */
    private String parent;
    
    /**
     * Табельный номер руководителя.
     */
    private String chiefPersonalNumber;
    
}
