package ru.sberbank.ditsib.transport.sudir.business.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Бизнеи-сущность роли.
 */
@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    
    /**
     * Код роли.
     */
    @EqualsAndHashCode.Include
    private final String code;
    
    /**
     * Название роли.
     */
    private final String name;
    
    /**
     * Описание роли.
     */
    private final String description;

}
