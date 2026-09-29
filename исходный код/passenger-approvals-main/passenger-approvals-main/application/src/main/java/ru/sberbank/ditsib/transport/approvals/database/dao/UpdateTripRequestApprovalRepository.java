package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.UpdateTripRequestApproval;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с согласованиями изменений поездок.
 */
public interface UpdateTripRequestApprovalRepository extends JpaRepository<UpdateTripRequestApproval, UUID>  {
    /**
     * Поиск согласования по идентификатору поездки.
     *
     * @param actionId Идентификатор поездки.
     *
     * @return согласование.
     */
    Optional<UpdateTripRequestApproval> findByActionId(UUID actionId);
    
    /**
     * Поиск согласования по идентификатору изменения.
     *
     * @param updateId Идентификатор изменения.
     *
     * @return согласование.
     */
    Optional<UpdateTripRequestApproval> findByUpdateId(UUID updateId);
    
    /**
     * Получение списка согласований по департаменту пассажира, статусам согласования и типу транспорта, если он передан
     * @param departmentIds id департамента
     * @param statuses статусы согласований
     * @param transportType тип транспорта. Мжожет быть null
     * @return список согласований
     */
    @Query("select t from UpdateTripRequestApproval t " +
           "inner join Employee e ON e.id = t.actorId " +
           "where t.status in (:statuses) and e.departmentId in (:departmentIds) " +
           "and (:transportType is null or t.transportType = :transportType) order by t.creationTime desc"
    )
    List<UpdateTripRequestApproval> getByDepartmentsInStatuses(Iterable<UUID> departmentIds, Iterable<Status> statuses,
                                                               @Nullable String transportType);


    /**
     * Получение списка согласований в определнных статусах для заявки
     * @param requestId id заявки
     * @param statuses статусы согласований
     * @return список согласований
     */
    List<UpdateTripRequestApproval> getByActionIdAndStatusIn(UUID requestId, Collection<Status> statuses);
    
}
