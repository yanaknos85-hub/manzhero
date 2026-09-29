package ru.sber.transport.approvals.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Сообщение с настройками согласований
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicApprovalsSettingsMessage extends ApprovalsSettingsMessage {
    
    /** Необходимость прикрепления документа на этапе "Создание" (OT) */
    private boolean approvalDocumentCheck;
    
    /** Необходимость этапа утверждения (OT) */
    private boolean affirmativeActive;
    
    /** Необходимость этапа подтверждения (OT) */
    private boolean tripConfirmationActive;
    
    /** Необходимость проверки документа на этапе "Подтверждение поездки" (OT) */
    private boolean tripConfirmationDocumentCheck;
}
