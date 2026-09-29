package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.approvals.database.model.SharedRideJoinApproval;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с согласованиями присоединенями к совместным поездкам на личном транспорте.
 */
public interface SharedRideApprovalRepository extends JpaRepository<SharedRideJoinApproval, UUID> {
    /**
     * Поиск согласования по идентификатору поездки.
     *
     * @param actionId Идентификатор поездки.
     *
     * @return согласование.
     */
    Optional<SharedRideJoinApproval> findByActionId(UUID actionId);
    
    /**
     * Поиск согласований для пользователя
     *
     * @param userId Идентификатор пользоваеля.
     *
     * @return согласования.
     */
    @Query("select t from SharedRideJoinApproval t " +
           "inner join Employee e ON e.id = t.actorId " +
           "where e.userId = :userId and t.status in (:statuses) order by t.creationTime desc"
    )
    List<SharedRideJoinApproval> findForUserInStatuses(UUID userId, Iterable<Status> statuses);
    

    Optional<SharedRideJoinApproval> getByAddRequestId(UUID addedRequestId);
}
