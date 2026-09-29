package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.Delegate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с делегатами
 */
public interface DelegateService {
    
    /**
     * Поиск делегата по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return делегат.
     */
    Optional<Delegate> get(UUID id);

    /**
     * Удаление делегата.
     *
     * @param delegate Делегат на удаление.
     */
    void delete(Delegate delegate);
    
    /**
     * Сохранение делегата.
     *
     * @param delegate делегат.
     */
    void save(Delegate delegate);
    
    /**
     * Получить сотрудников, кому делегировал полномочия указанный..
     *
     * @param supervisorEmployeeId сотрудник.
     *
     * @return делегат.
     */
    Set<Delegate> getDelegatesBySupervisor(UUID supervisorEmployeeId);
    
}
