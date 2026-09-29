package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Тариф заправки топливом
 */
@Entity
@Table(name = "fuel_tariff")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FuelTariff {

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
     * Идентификатор подразделения
     */
    @NotNull
    private UUID departmentId;

    /**
     * Скидка от розничной сети
     */
    @NotNull
    private BigDecimal discount;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (FuelTariff) o;
        return tariffId != null && Objects.equals(tariffId, that.tariffId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
