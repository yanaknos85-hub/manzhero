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
 * Сотрудник
 */
@Entity
@Table(schema = "approvals", name = "message_employee")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Employee {

    /**
     * Идентификатор записи о сотруднике
     */
    @Id
    private UUID id;

    /**
     * Идентификатор записи с таблицы corporate.user
     */
    @Column(unique = true)
    private UUID userId;

    /**
     * Имя
     */
    @NotBlank
    private String firstName;

    /**
     * Фамилия
     */
    @NotBlank
    private String lastName;

    /**
     * Отчество
     */
    private String patronymic;

    /**
     * Табельный номер
     */
    @NotBlank
    private String personnelNumber;

    /**
     * Идентификатор сотрудника, делегатом которого является
     */
    private UUID delegatedBy;

    /**
     * Человекочитаемый идентификатор
     */
    @NotBlank
    @Column(nullable = false)
    private String humanReadableId;

    /**
     * Начальник
     */
    private UUID supervisorId;

    /**
     * Отдел
     */
    @NotNull
    private UUID departmentId;

    /**
     * Должность
     */
    @NotNull
    private UUID positionId;

    /**
     * Список подразделений, которые может согласовывать данный сотрудник
     */
    @ElementCollection
    @CollectionTable(schema = "approvals", name = "approvers",
            joinColumns = @JoinColumn(name = "employee_id"))
    private Collection<ApproveDepartment> approveDepartments = new ArrayList<>();

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
        var that = (Employee) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
