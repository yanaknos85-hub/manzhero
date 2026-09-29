package ru.sberbank.ditsib.corpclient.dto.purpose;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект данных с целями поездки.
 */
@Getter
@Setter
public class PurposeFileDto {
    
    /**
     * Название цели.
     */
    private String name;
    
    /**
     * Организация.
     */
    private String organization;
    
    /**
     * Флаг активности.
     */
    private boolean active;
    
    /**
     * Тип цели.
     */
    private String type;
    
}
