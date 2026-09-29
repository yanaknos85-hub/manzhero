package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Должность
 */
@Entity
@Table(name = "position")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Position {
    
    /**
     * Идентификатор записи о должности
     */
    @Id
    private UUID id;
    
    /**
     * Идентификатор записи об организации
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Наименование позиции
     */
    @NotBlank
    private String positionName;
    
    /**
     * Флаг активности
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
        var that = (Position) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
}
