package ru.sber.transport.approvals.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.util.Collection;
import java.util.UUID;

/**
 * Сотрудники, имеюшие право согласовывать и редактировать заявки подразделения
 *
 * @param departmentId Подразделение, заявки которого могут согласовывать согласующие
 * @param approvers    согласующие
 */
public record DepartmentTripRequestApproversMessage(
        UUID departmentId,
        Collection<Approver> approvers
) implements Message<UUID> {

    @JsonIgnore
    @Override
    public UUID getId() {
        return departmentId;
    }

    /**
     * Согласующий может согласовывать заявки только данного типа транспорта.
     * Null если ограничений нет.
     */
    public record Approver(
            UUID employeeId,
            String transportType
    ) {
    }
}