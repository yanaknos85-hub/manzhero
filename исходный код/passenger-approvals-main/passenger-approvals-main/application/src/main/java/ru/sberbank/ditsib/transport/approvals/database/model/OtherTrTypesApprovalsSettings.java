package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

/**
 * Сущность настроек согласования поездок для всех типов транспорта, кроме такси и общественного
 */
@Entity
@Getter
@DiscriminatorValue("OTHER")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OtherTrTypesApprovalsSettings extends ApprovalsSettings {
    
    /** Необходимость этапа утверждения поездки */
    @Builder.Default
    private boolean tripApprovalActive = true;
}
