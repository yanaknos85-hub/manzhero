package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сообщение сотрудника.
 */
@Jacksonized
@Builder
@Getter
public class EmployeeMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private final UUID id;
    
    /**
     * ID связанного пользователя.
     */
    private final UUID userId;
    
    /**
     * Человекочитаемый ID.
     */
    private final String humanReadableId;
    
    /**
     * Имя.
     */
    private final String firstName;
    
    /**
     * Фамилия.
     */
    private final String lastName;
    
    /**
     * Отчество.
     */
    private final String patronymic;
    
    /**
     * ТН.
     */
    private final String personnelNumber;
    
    /**
     * ID подразделения.
     */
    private final UUID departmentId;

    /**
     * Место возникновения затрат
     */
    private final String costCenter;

    /**
     * Разъездной характер.
     */
    private final String itinerantType;
    
    /**
     * ID организации.
     */
    private final UUID organizationId;
    
    /**
     * ID должности.
     */
    private final UUID positionId;
    
    /**
     * isDelegatedTrait
     */
    private final boolean delegateTrait;
    
    /**
     * ID делегата.
     */
    private final UUID delegatedById;
    
    /**
     * Доступные типы транспорта.
     */
    @Builder.Default
    private final Set<String> availableTransportTypes = new HashSet<>();
    
    /**
     * Номер телефона.
     */
    private final String mobilePhone;
    
    /**
     * Email.
     */
    private final String email;

    /**
     * Свидетельство о браке.
     */
    private final String marriageCertificateNumber;
    
    /**
     * ID руководителя.
     */
    private final UUID supervisorId;
    
    /**
     * External or internal employee type
     */
    private final String employeeType;

    /**
     * Пол сотрудника.
     */
    private final String gender;
    
    /**
     * Consent for PND
     */
    private final boolean consent;
    
    /**
     * Флаг удаленного пользователя.
     */
    @Builder.Default
    private final boolean deleted = false;
    
    /**
     * Атрибуты сотрудника.
     */
    @Builder.Default
    private final Set<String> attributes = new HashSet<>();
}
