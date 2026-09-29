package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Автосервис
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "service_point")
public class ServicePoint {
    
    /**
     * Идентификатор записи об автосервисе
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Адрес автосервиса
     */
    @NotBlank
    private String address;
    
    /**
     * Широта
     */
    @NotNull
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    
    /**
     * Долгота
     */
    @NotNull
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
    
    /**
     * Идентификатор договора
     */
    private UUID contractId;

    /**
     * Метка активности
     */
    @Builder.Default
    private boolean active = true;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (ServicePoint) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
