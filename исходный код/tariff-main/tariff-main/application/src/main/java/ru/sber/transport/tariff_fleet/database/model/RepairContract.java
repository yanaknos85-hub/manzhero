package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Договор Ремонт
 */
@Entity
@Table(name = "repair_contract")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RepairContract extends AbstractContract {
    
    /**
     * Договор
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    /**
     * Идентификатор записи об контрагенте
     */
    @NotNull
    private UUID contractorId;

    /**
     * Контрагент
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractorId", referencedColumnName = "id", insertable = false, updatable = false)
    private Contractor contractor;
    
    /**
     * Наименование для системных точек
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String servicePointsName;
    
    /**
     * Сумма контракта (с НДС)
     */
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    private Long amountWithVat;
    
    /**
     * Сумма договора (без НДС)
     */
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    private Long amountWithoutVat;
    
    /**
     * Идентификатор записи об организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
    private Organization organization;
    
    /**
     * Идентификатор файла в хранилище S3
     */
    @Column(name = "logo_s3_id")
    private UUID logoS3Id;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "contractId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServicePoint> servicePoints = new ArrayList<>();
    
    @PrePersist
    private void onCreation() {
        if (servicePoints != null) {
            servicePoints.forEach(station -> station.setContractId(this.getContractId()));
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (RepairContract) o;
        return contractId != null && Objects.equals(contractId, that.contractId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
