package ru.sber.transport.corporate.web.resolvers.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Объект обмена данными через файл.
 */
@Getter
@Setter
@ToString
public class FileEmployee {
    
    /**
     * Организация.
     */
    private String organization;
    
    /**
     * Подразделение.
     */
    private String department;
    
    /**
     * Табельный номер.
     */
    @NotNull
    private String personalNumber;
    
    /**
     * Роль.
     */
    private String role;
    
    /**
     * ФИО.
     */
    private String fullName;
    
    /**
     * E-Mail.
     */
    @NotNull
    private String email;
    
    /**
     * Номера телефонов через запятую.
     */
    private String phone;
    
    /**
     * Должность.
     */
    private String position;
    
    /**
     * ТН руководителя.
     */
    private String supervisorPersonalNumber;
}
