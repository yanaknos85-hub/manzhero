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
public class OtherTrTypesApprovalsSettingsMessage extends ApprovalsSettingsMessage {
    
    /** Необходимость этапа утверждения поездки */
    private boolean tripApprovalActive;
}
