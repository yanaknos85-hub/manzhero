package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Организация
 */
@Entity
@Table(schema = "approvals", name = "message_organization")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {

    /**
     * Идентификатор записи об организации
     */
    @Id
    private UUID id;

    /**
     * Уникальный идентификатор (числовой)
     */
    @NotNull
    @Column(columnDefinition = "numeric")
    private Long digitId;

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
        var that = (Organization) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
