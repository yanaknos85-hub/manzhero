package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.approvals.database.model.Delegate;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий для работы с делегатами.
 */
public interface DelegateRepository extends JpaRepository<Delegate, UUID> {
    
    /**
     * Получить всех делегатов по идентификатору делегирующего.
     *
     * @param supervisorId идентификатор делегирующего.
     * @param date дата для выборки.
     *
     * @return список делегатов.
     */
    @Query("SELECT delegate FROM Delegate delegate WHERE delegate.supervisorId = :supervisorId " +
           "AND delegate.startDate <= :date AND :date <= delegate.endDate")
    Set<Delegate> findAllBySupervisorId(UUID supervisorId, LocalDate date);


}
