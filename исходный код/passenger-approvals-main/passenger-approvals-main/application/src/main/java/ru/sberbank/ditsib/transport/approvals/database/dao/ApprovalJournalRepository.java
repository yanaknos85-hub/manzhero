package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.approvals.database.model.ApprovalJournal;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий согласований.
 */
public interface ApprovalJournalRepository extends JpaRepository<ApprovalJournal, UUID> {

    Optional<ApprovalJournal> findApprovalJournalByApprovalId(UUID approvalId);

    /**
     * Считает количество согласований для журнала
     *
     * @param statuses          статусы для фильтрации
     * @param employeeIds       идентификаторы пользователей, которые являются участниками согласований
     * @param allTransportTypes ищем ли по любым типа транспорта
     * @param transportTypes    типы транспорта для фильтрации
     * @param userId            идентификатор пользователя в таблице corporate.user
     * @param pageable          настройки пагинации
     * @return {@link Page<ApprovalJournalProjection>}
     */
    @Query(value = """
            SELECT
                a.approvalId AS id,
                me.id AS employeeId,
                me.firstName AS employeeFirstName,
                me.lastName AS employeeLastName,
                me.patronymic AS employeePatronymic,
                me.personnelNumber AS employeePersonnelNumber,
                me.departmentId AS employeeDepartmentId,
                me.userId AS employeeUserId,
                a.actionId AS requestId,
                a.transportType AS transportType,
                a.taxiClass AS taxiClass,
                a.desiredDate AS desiredDate,
                tp.id AS purposeId,
                tp.label AS purposeLabel,
                a.expectedCost AS cost,
                CAST(a.status AS text) AS status,
                a.waypoints AS waypoints,
                a.expectedTime AS expectedTime,
                a.expectedDistance AS expectedDistance,
                a.passengerCount AS passengerCount,
                a.humanReadableId AS requestHumanReadableId,
                CASE WHEN a.sharedRideId IS NOT NULL THEN TRUE ELSE FALSE END AS isCoopTrip,
                CAST(a.type AS text) as type,
                a.timeZone AS timeZone,
                a.addRequestId AS addRequestId
            FROM ApprovalJournal a
                INNER JOIN Employee me ON me.id = a.actorId
                INNER JOIN TripPurpose tp ON tp.id = a.tripPurposeId
            WHERE a.actorId in (:employeeIds)
                AND a.status in (:statuses)
                AND (:allTransportTypes = true OR a.transportType IN (:transportTypes))
            """,
            countQuery = """
            SELECT COUNT(a) FROM ApprovalJournal a
            WHERE a.actorId IN (:employeeIds)
                AND a.status in (:statuses)
                AND (:allTransportTypes = true OR a.transportType IN (:transportTypes))
            """)
    Page<ApprovalJournalProjection> findAllForJournal(
            List<String> statuses,
            List<UUID> employeeIds,
            boolean allTransportTypes,
            Set<String> transportTypes,
            UUID userId,
            Pageable pageable
    );

    /**
     * Считает количество согласований для журнала
     *
     * @param statuses          статусы для фильтрации
     * @param employeeIds       идентификаторы пользователей, которые являются участниками согласований
     * @param allTransportTypes ищем ли по любым типа транспорта
     * @param transportTypes    типы транспорта для фильтрации
     * @param userId            идентификатор пользователя в таблице corporate.user
     * @return количество согласований
     */
    @Query("""
            SELECT COUNT(a) FROM ApprovalJournal a
            WHERE a.actorId IN (:employeeIds)
                AND a.status in (:statuses)
                AND (:allTransportTypes = true OR a.transportType IN (:transportTypes))
            """)
    long countForJournal(
            List<String> statuses,
            List<UUID> employeeIds,
            boolean allTransportTypes,
            Iterable<String> transportTypes,
            UUID userId
    );
}
