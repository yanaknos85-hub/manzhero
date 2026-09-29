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
 * Подразделение
 */
@Entity
@Table(name = "department")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Department {

    /**
     * Идентификатор записи о подразделении
     */
    @Id
    private UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    @NotBlank
    private String humanReadableId;

    /**
     * Идентификатор записи об организации
     */
    @NotNull
    private UUID organizationId;

    /**
     * Идентификатор записи родителя в таблице department
     */
    private UUID parentId;

    /**
     * Наименование подразделения
     */
    @NotBlank
    private String departmentName;

    /**
     * Флаг активности
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Орг. единица
     */
    private String easupId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Department) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
