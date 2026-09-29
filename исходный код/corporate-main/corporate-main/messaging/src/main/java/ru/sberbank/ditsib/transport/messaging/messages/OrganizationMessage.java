package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение с данными организации.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMessage implements Message<UUID> {
    
    /**
     * Идентификатор организации.
     */
    private UUID id;
    
    /**
     * Цифровой идентификатор.
     */
    private Long digitId;
    
    /**
     * Название организации.
     */
    private String officialName;
    
    /**
     * Адрес организации.
     */
    private String address;
    
    /**
     * ОГРН.
     */
    private String msrn;
    
    /**
     * ИНН.
     */
    private String tid;
    
    /**
     * Код организационной единицы
     */
    private Integer organizationCode;

    /**
     * Список контактов.
     */
    private List<ContactMessage> contacts = new ArrayList<>();
    
    /**
     * Флаг об удалении организации.
     */
    private boolean deleted = false;

    /**
     * Группа организаций
     */
    private OrganizationGroup organizationGroup;

    /**
     * Доступные типы транспорта
     */
    private List<String> availableClasses = new ArrayList<>();

    public record OrganizationGroup(
        UUID id,
        String name,
        boolean internal
    ){}
}
