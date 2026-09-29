package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * Оператор ЭДО
 */
@Entity
@Table(name = "edf_operator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EdfOperator {
    
    /**
     * Идентификатор записи об операторе ЭДО
     */
    @Id
    @NotBlank
    @Size(min = 1, max = 10)
    private String id;
    
    /**
     * Наименование
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
    
    /**
     * Отображаемое наименование
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    
    /**
     * Флаг активности
     */
    private boolean active = true;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (EdfOperator) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
