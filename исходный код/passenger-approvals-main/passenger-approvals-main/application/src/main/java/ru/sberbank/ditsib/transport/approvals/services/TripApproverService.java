package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.Approver;

import java.util.Collection;
import java.util.UUID;

/**
 * Сервис для работы с согласующими заявок на поездки.
 * <ul>
 * <li>Есть метод {@link TripApproverService#computeApprovers} поиска списка согласующих для подразделения, а так
 * <li>Есть методы обновления списка согласующих в базе (т.е. по сути кеша списка согласующих, чтобы не рассчитывать
 * </ul>
 * его каждый раз заново).
 */
public interface TripApproverService {
    
    /**
     * Получение согласующих. Производит поиск по базе, не из сохраненного в базе списка согласующих
     *
     * @param employeeId сотрудник, согласующих которого нужно получить.
     * @return согласующие.
     */
    Collection<Approver> computeApprovers(UUID employeeId);
    
    /**
     * Обновление согласующих при создании/удалении/изменении делегата
     * @param supervisorId - то, что прописано в делегате. Нельзя использовать id delegate, так как он может быть удален
     */
    void onDelegateChanged(UUID supervisorId);

    /**
     * Обновление согласующих при создании/изменении подразделения
     * @param departmentId - id затронутого департамента
     */
    void onDepartmentChanged(UUID departmentId);
}
