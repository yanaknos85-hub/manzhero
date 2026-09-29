package ru.sberbank.ditsib.transport.approvals.services;

import java.util.UUID;

/**
 * Сервис для внедрения настроек согласований
 */
public interface ApprovalsSettingsInjectionService {
    
    /**
     * Получить флаг автосогласования после проверки по настройкам
     *
     * @param tariffId ID тарифа
     * @param transportType тип транспорта
     * @param costRub стоимость в руб.
     * @param purposeId ID цели поездки
     * @param employeeId ID сотрудника из заявки (для вычисления organizationId)
     * @param requestId ID заявки
     *
     * @return флаг автосогласования
     */
    boolean isAutoApproveByApprovalsSettings(
            UUID tariffId, String transportType, double costRub,
            UUID purposeId, UUID employeeId, UUID requestId
    );

    /**
     * Получить флаг необходимости прикрепления документа на этапе подтверждения и просмотра на этапе утверждения
     *
     * @param employeeId ID сотрудника из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrConfirmationDocumentRequired(UUID employeeId);
    
    /**
     * Получить флаг необходимости этапа утверждения поездки согласующим
     *
     * @param employeeId ID сотрудника из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrAffirmativeRequired(UUID employeeId);
    
    /**
     * Получить флаг необходимости этапа утверждения финального маршрута
     *
     * @param employeeId ID сотрудника из заявки (для вычисления organizationId)
     * @param transportType тип транспорта
     *
     * @return значение настройки
     */
    boolean isOtherTrFinalTripConfirmation(UUID employeeId, String transportType);
}
