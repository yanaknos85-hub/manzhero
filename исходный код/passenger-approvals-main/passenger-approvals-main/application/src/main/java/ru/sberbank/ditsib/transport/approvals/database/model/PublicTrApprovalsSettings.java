package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Сущность настроек согласования поездок на общественном траспорте
 */
@Entity
@Getter
@DiscriminatorValue("PUBLIC")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PublicTrApprovalsSettings extends ApprovalsSettings {
    
    /**
     * Необходимость прикрепления документа на этапе "Создание"  (для междугородних поездок)
     */
    @Builder.Default
    @Column(name = "approval_document_check")
    private boolean approvalDocumentCheck = true;
    
    /**
     * Необходимость этапа утверждения
     */
    @Builder.Default
    @Column(name = "affirmative_active")
    private boolean affirmativeActive = true;
    
    /**
     * Необходимость этапа подтверждения
     */
    @Builder.Default
    @Column(name = "trip_confirmation_active")
    private boolean tripConfirmationActive = true;
    
    /**
     * Необходимость прикрепления документа на этапе "Подтверждение поездки" (для междугородних поездок)
     */
    @Builder.Default
    @Column(name = "trip_confirmation_document_check")
    private boolean tripConfirmationDocumentCheck = true;
}
