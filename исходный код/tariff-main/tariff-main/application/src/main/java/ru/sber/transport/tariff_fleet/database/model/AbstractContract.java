package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractContract implements Serializable {
    /**
     * Идентификатор записи о договоре
     */
    @Id
    protected UUID contractId;

    protected abstract Contract getContract();

    @PrePersist
    @PreUpdate
    private void syncContractId() {
        if (getContract() != null && contractId == null) {
            contractId = getContract().getId();
        }
    }

}
