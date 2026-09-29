package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

/**
 * Содержит в себе подразделение и тип транспорта, заявки которые может согласовывать сотрудник
 */
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
@Getter
@EqualsAndHashCode(of = {"departmentId", "transportType"})
public class ApproveDepartment {

    /**
     * Подразделение, заявки которого разрешено согласовывать сотруднику
     */
    @Column(name = "department_id")
    private UUID departmentId;

    /**
     * Ограничение на согласование по типу транспорта (null для head, заполнено для делегата)
     */
    @Column(name = "transport_type")
    private String transportType;
}
