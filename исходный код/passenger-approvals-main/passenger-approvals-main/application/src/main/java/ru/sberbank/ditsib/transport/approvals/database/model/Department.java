package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Должность
 */
@Entity
@Table(schema = "approvals", name = "message_department")
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
     * Идентификатор записи об организации
     */
    @NotNull
    private UUID organizationId;

    /**
     * Идентификатор записи родителя в таблице department
     */
    private UUID parentId;

    /**
     * Головной отдел
     */
    private UUID departmentHeadId;

    /**
     * Список согласующих, которые могут согласовать заявки данного подразделения
     */
    @ElementCollection
    @CollectionTable(schema = "approvals", name = "approvers",
            joinColumns = @JoinColumn(name = "department_id"))
    private Collection<Approver> approvers = new ArrayList<>();

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
