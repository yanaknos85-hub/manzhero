package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Тариф ЭПЛ
 */
@Entity
@Table(name = "ewb_tariff")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EwbTariff {
    
    /**
     * Идентификатор записи о тарифе
     */
    @Id
    private UUID tariffId;
    
    /**
     * Тариф
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;
    
    /**
     * Идентификатор записи об организации контрагента
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Идентификатор записи о подразделении контрагента
     */
    @NotNull
    private UUID departmentId;
    
    /**
     * Стоимость осмотра
     */
    @NotNull
    @Max(value = 999999L)
    @PositiveOrZero
    private Long amount;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (EwbTariff) o;
        return tariffId != null && Objects.equals(tariffId, that.tariffId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
