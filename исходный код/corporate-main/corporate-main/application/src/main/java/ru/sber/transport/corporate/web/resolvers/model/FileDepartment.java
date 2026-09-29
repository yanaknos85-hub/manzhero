package ru.sber.transport.corporate.web.resolvers.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект подразделения из файла.
 */
@Getter
@Setter
@NoArgsConstructor
public class FileDepartment {
    
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
