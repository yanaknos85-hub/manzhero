package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

/**
 * Согласующий
 */
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
@Getter
@EqualsAndHashCode(of = {"employeeId", "transportType"})
public class Approver {
    
    /**
     * Сотрудник, имеющий право согласовывать
     */
    @Column(name = "employee_id")
    private UUID employeeId;
    
    /**
     * Ограничение на согласование по типу транспорта (null для head, заполнено для делегата)
     */
    @Column(name = "transport_type")
    private String transportType;
}
