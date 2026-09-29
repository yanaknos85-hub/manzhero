package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Сущность настроек согласования поездок на групповой трансфер
 */
@Entity
@DiscriminatorValue(value = "GROUP_TRANSFER")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GroupTransferApprovalsSettings extends ApprovalsSettings {
}
