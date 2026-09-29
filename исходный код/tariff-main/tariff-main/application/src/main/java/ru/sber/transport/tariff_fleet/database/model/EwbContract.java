package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import ru.sber.transport.tariff_fleet.constant.InspectionType;

import java.util.Objects;
import java.util.UUID;

/**
 * Договор ЭПЛ
 */
@Entity
@Table(name = "ewb_contract")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EwbContract extends AbstractContract {

    /**
     * Договор
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    /**
     * Идентификатор записи об организации контрагента
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Сумма договора (без НДС)
     */
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    private Long amount;
    
    /**
     * Вид осмотра
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private InspectionType inspectionType;
    
    /**
     * Идентификатор записи об операторе ЭДО
     */
    @NotBlank
    @Size(min = 1, max = 10)
    private String edfOperatorId;
    
    /**
     * Код участника
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String edfCode;
    
    /**
     * Медицинская лицензия организации
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_medical_license_id", referencedColumnName = "id")
    private OrganizationMedicalLicense organizationMedicalLicense;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (EwbContract) o;
        return contractId != null && Objects.equals(contractId, that.contractId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
