package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.approvals.database.model.FinalTripApproval;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с согласованиями финальных поездок (Утверждение финального маршрута).
 */
public interface FinalTripApprovalRepository extends JpaRepository<FinalTripApproval, UUID> {
    /**
     * Поиск согласования по идентификатору поездки.
     *
     * @param actionId Идентификатор поездки.
     *
     * @return согласование.
     */
    Optional<FinalTripApproval> findByActionId(UUID actionId);
    
    /**
     * Получение списка согласований по департаменту пассажира, статусам согласования и типу транспорта, если он передан
     * @param departmentIds id департамента
     * @param statuses статусы согласований
     * @param transportType тип транспорта. Мжожет быть null
     * @return список согласований
     */
    @Query("select t from FinalTripApproval t " +
           "inner join Employee e ON e.id = t.actorId " +
           "where t.status in (:statuses) and e.departmentId in (:departmentIds) " +
           "and (:transportType is null or t.transportType = :transportType) order by t.creationTime desc"
    )
    List<FinalTripApproval> getByDepartmentsInStatuses(Iterable<UUID> departmentIds, @NonNull Iterable<Status> statuses,
                                                       @Nullable String transportType);


}
