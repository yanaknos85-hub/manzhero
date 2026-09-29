package ru.sberbank.ditsib.corpclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект данных об аттрибутах из файла.
 */
@Setter
@Getter
@NoArgsConstructor
public class AttributesFileDTO {
    
    /**
     * Организация.
     */
    private String organization;
    
    /**
     * Табельный номер.
     */
    private String personNumber;
    
    /**
     * Аттрибуты.
     */
    private String attributes;
    
}
