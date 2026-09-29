package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с согласованиями поездок.
 */
public interface TripRequestApprovalRepository extends JpaRepository<TripRequestApproval, UUID>, JpaSpecificationExecutor<TripRequestApproval> {
    
    /**
     * Поиск согласования по идентификатору поездки.
     *
     * @param actionId Идентификатор поездки.
     *
     * @return согласование.
     */
    Optional<TripRequestApproval> findByActionId(UUID actionId);

    /**
     * Поиск согласования по идентификатору сотрудника и статусу.
     *
     * @return Not approved approvals for actor
     */
    List<TripRequestApproval> findByActorIdAndStatusIn(UUID actorId, Iterable<Status> statuses);
    
    // todo в случае проблем с производительностью у методов, ищущих согласования по статусам и по actor,
    // можно будет попробовать добавить еще один параметр 'desired_date', чтобы не смотреть
    // согласования в прошлом (для поля desired_date уже добавил индекс)
    
    /**
     * Получение списка согласований по должности пассажира и стутсам согласования
     *
     * @param positionId id позиции
     * @param statuses статусы согласований
     *
     * @return список согласований
     */
    @Query("select t from TripRequestApproval t " +
           "inner join Employee e ON e.id = t.actorId " +
           "where t.status in (:statuses) and e.positionId = :positionId"
    )
    List<TripRequestApproval> findByPositionAndStatus(UUID positionId, Iterable<Status> statuses);
    
    /**
     * Получение списка согласований по департаменту пассажира и стутсам согласования
     *
     * @param departmentId id департамента
     * @param statuses статусы согласований
     *
     * @return список согласований
     */
    @Query("select t from TripRequestApproval t " +
           "inner join Employee e ON e.id = t.actorId " +
           "where t.status in (:statuses) and e.departmentId = :departmentId"
    )
    List<TripRequestApproval> findByDepartment(UUID departmentId, Iterable<Status> statuses);

    String queryInStatus = "select t from TripRequestApproval t " +
            "inner join Employee e ON e.id = t.actorId " +
            "where t.status in (:statuses) and e.departmentId in (:departmentIds) " +
            "     and (:transportTypeList is null or t.transportType in (:transportTypeList)) order by t.creationTime" +
                           " desc";

    /**
     * Получение списка согласований по департаменту пассажира, статусам согласования и типу транспорта, если он передан
     *
     * @param departmentIds id департамента
     * @param statuses статусы согласований
     * @param transportTypeList тип транспорта.
     * @return список согласований
     */
    @Query(queryInStatus)
    Page<TripRequestApproval> getByDepartmentsInStatuses(
            Iterable<UUID> departmentIds, Iterable<Status> statuses,
            Iterable<String> transportTypeList,
            Pageable page
    );

    /**
     * Поиск владельца личного транспорта в совместной поездке
     *
     * @param sharedRideId идентификатор совместной поездки
     *
     * @return согласование
     */
    @Query("select t from TripRequestApproval t where " +
           "t.sharedRideId = :sharedRideId and " +
           "t.sharedRideOwner = true and " +
           "t.transportType = 'PERSONAL'")
    Optional<TripRequestApproval> findSharedRideOwner(UUID sharedRideId);
}
