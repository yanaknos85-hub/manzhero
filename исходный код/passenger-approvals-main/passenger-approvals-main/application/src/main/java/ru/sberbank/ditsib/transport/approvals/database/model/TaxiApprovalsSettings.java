package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Сущность настроек согласования поездок на такси
 */
@Entity
@DiscriminatorValue(value = "TAXI")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class TaxiApprovalsSettings extends ApprovalsSettings {
}
