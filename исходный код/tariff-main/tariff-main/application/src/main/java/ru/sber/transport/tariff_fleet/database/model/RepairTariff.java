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
 * Тариф ремонт
 */
@Entity
@Table(name = "repair_tariff")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RepairTariff {

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
     * Выездной сервис
     */
    private boolean isFieldService;

    /**
     * Стоимость нормо-часа, руб
     */
    private int hourNormalizedPrice;

    /**
     * Размер скидки на запасные части, %
     */
    private BigDecimal detailDiscountPrice;

    /**
     * Гарантия на работы по времени, месяцы
     */
    private int workWarranty;

    /**
     * Гарантия на работы по пробегу, км
     */
    private int mileageWarranty;

    /**
     * Гарантия на запчасти, месяцы
     */
    private int detailWarranty;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (RepairTariff) o;
        return tariffId != null && Objects.equals(tariffId, that.tariffId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
