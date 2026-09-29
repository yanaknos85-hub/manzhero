package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord_;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of delegates
 */
public interface DelegateRepository extends JpaSpecificationExecutor<DelegateRecord>,
        JpaRepository<DelegateRecord, UUID> {

    /**
     * Check unique constraint
     *
     * @param supervisorId supervisor id
     * @param delegateId   delegate id
     * @param from         date from
     * @return true if data is not unique
     */
    @Query("SELECT COUNT(delegate) > 0 FROM DelegateRecord delegate " +
            "WHERE (delegate.supervisor.id = :supervisorId and delegate.delegate.id = :delegateId " +
            "and delegate.transportType = :transportType " +
            "and delegate.startDate = :from) and delegate.status = 'ACTIVE'")
    boolean existsByUniqueFields(UUID supervisorId, UUID delegateId, TransportTypeEnum transportType, LocalDate from);

    /**
     * Check unique constraint
     *
     * @param supervisorId supervisor id
     * @param delegateId   delegate id
     * @param from         date from
     * @return true if data is not unique
     */
    @Query("SELECT COUNT(delegate) > 0 FROM DelegateRecord delegate " +
            "WHERE delegate.id <> :id AND (delegate.supervisor.id = :supervisorId and delegate.delegate.id = :delegateId" +
            " " +
            "and delegate.transportType = :transportType " +
            "and delegate.startDate = :from) and delegate.status = 'ACTIVE'")
    boolean existsByUniqueFieldsExceptSame(
            UUID id, UUID supervisorId, UUID delegateId, TransportTypeEnum transportType,
            LocalDate from
    );


    /**
     * Check range constraint
     *
     * @param supervisorId supervisor id
     * @param delegateId   delegate id
     * @param from         date from
     * @param to           date to
     * @return true if date range crosses with another date range for same supervisor-delegate pair
     */
    @Query("SELECT COUNT(delegate) > 0 FROM DelegateRecord delegate " +
            "WHERE (delegate.supervisor.id = :supervisorId and delegate.delegate.id = :delegateId " +
            "and delegate.transportType = :transportType " +
            "and ((delegate.startDate <= :from and delegate.endDate>=:from) " +
            "or (delegate.endDate >= :from and delegate.startDate<=:to))) and delegate.status = 'ACTIVE'")
    boolean existsByIntervalDates(
            UUID supervisorId, UUID delegateId, TransportTypeEnum transportType, LocalDate from,
            LocalDate to
    );

    /**
     * Check range constraint, except recore with id
     *
     * @param id           id delegate for except
     * @param supervisorId supervisor id
     * @param delegateId   delegate id
     * @param from         date from
     * @param to           date to
     * @return true if date range crosses with another date range for same supervisor-delegate pair
     */
    @Query("SELECT COUNT(delegate) > 0 FROM DelegateRecord delegate " +
            "WHERE delegate.id <> :id AND (delegate.supervisor.id = :supervisorId " +
            "and delegate.delegate.id = :delegateId " +
            "and delegate.transportType = :transportType " +
            "and ((delegate.startDate <= :from and delegate.endDate>=:from) " +
            "or (delegate.endDate >= :from and delegate.startDate<=:to))) and delegate.status = 'ACTIVE'")
    boolean existsByIntervalDatesExceptSame(
            UUID id, UUID supervisorId, UUID delegateId, TransportTypeEnum transportType,
            LocalDate from,
            LocalDate to
    );

    /**
     * Get list of all delegates of specified supervisor
     *
     * @param supervisorId supervisor id
     * @return list of delegate records
     */
    @Query("SELECT delegate FROM DelegateRecord delegate WHERE delegate.supervisor.id = :supervisorId " +
            "AND delegate.status = 'ACTIVE' " +
            "ORDER BY delegate.delegate.id, delegate.startDate ASC")
    List<DelegateRecord> getAllBySupervisorIdOrderByDelegateIdAscStartDateAsc(UUID supervisorId);

    @Query("SELECT delegate FROM DelegateRecord delegate WHERE delegate.supervisor.id = :supervisorId " +
            "AND delegate.status = 'ACTIVE' " +
            "ORDER BY delegate.delegate.id, delegate.startDate ASC")
    Page<DelegateRecord> getAllBySupervisorIdOrderByDelegateIdAscStartDateAsc(UUID supervisorId, Pageable pageable);

    /**
     * Get list of all delegates records that specified employee participates in
     *
     * @param delegateId delegate id
     * @return list of delegate records
     */
    @Query("SELECT delegate FROM DelegateRecord delegate WHERE delegate.delegate.id = :delegateId " +
            "AND delegate.status = 'ACTIVE' " +
            "ORDER BY delegate.startDate")
    List<DelegateRecord> getAllByDelegateIdOrderByStartDate(UUID delegateId);

    /**
     * Get list of all delegates records that satisfy to parameters provided
     *
     * @param supervisorId  supervisor id
     * @param transportType transport type
     * @param date          date of delegation
     * @return list of delegate records
     */
    @Query("select delegate from DelegateRecord delegate where delegate.supervisor.id=:supervisorId and " +
            "delegate.transportType=:transportType and (delegate.startDate<=:date and " +
            "delegate.endDate>=:date) and delegate.status = 'ACTIVE'")
    List<DelegateRecord> getAllBySupervisorIdAndTransportIdAndDate(
            UUID supervisorId, TransportTypeEnum transportType, LocalDate date
    );

    /**
     * Get list of all delegates records that satisfy to parameters provided
     *
     * @param supervisorId supervisor id
     * @param date         date of delegation
     * @return list of delegate records
     */
    @Query("""
            select delegate from DelegateRecord delegate where delegate.supervisor.id=:supervisorId
             and (delegate.startDate<=:date and
             delegate.endDate>=:date) and delegate.status = 'ACTIVE'""")
    List<DelegateRecord> getAllBySupervisorIdAndDate(UUID supervisorId, LocalDate date);


    @Query("""
            from DelegateRecord delegate where delegate.supervisor.id=:supervisorId
             and (delegate.startDate<=:date and delegate.endDate>=:date) and delegate.status = 'ACTIVE'""")
    Page<DelegateRecord> getAllBySupervisorIdAndDate(UUID supervisorId, LocalDate date, Pageable pageable);

    /**
     * Get delegate by id
     *
     * @param organizationId   organization id
     * @param delegateRecordId delegate delegate id
     * @return optional of delegate
     */
    @Query("""
            select delegate from DelegateRecord delegate where delegate.supervisor.organization.id=:organizationId
            and delegate.id = :delegateRecordId
            and delegate.status = 'ACTIVE'""")
    Optional<DelegateRecord> findBySupervisorOrganizationIdAndId(
            @NotNull UUID organizationId,
            @NotNull UUID delegateRecordId
    );


    /**
     * Получить список делегированных полномочий указанному делегату
     *
     * @param delegateId ID делегата
     * @param date       дата на которую выбираются записи
     * @return список записей о делегатах
     */
    @Query("""
            select delegate from DelegateRecord delegate where delegate.delegate.id=:delegateId
             and (delegate.startDate<=:date and
             delegate.endDate>=:date) and delegate.status = 'ACTIVE' order by delegate.startDate""")
    List<DelegateRecord> getAllByDelegateIdAndDateOrderByStartDate(UUID delegateId, LocalDate date);

    /**
     * Получить список делегированных полномочий по статусу
     *
     * @param status status
     * @return список записей о делегатах
     */
    List<DelegateRecord> findByStatus(RecordStatus status);

}
