package ru.sberbank.ditsib.transport.approvals.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис по работе с согласованиями.
 *
 * @param <T> тип согласования.
 */
public interface ApproveService<T> {

    List<Status> APPROVABLE_STATUSES = Arrays.asList(Status.NEW, Status.EDITED);

    List<Status> CANCELLABLE_STATUSES = Arrays.asList(Status.NEW, Status.EDITED);

    /**
     * Отменить согласование. Если согласование не существует в базе, то данный метод ничего не сохраняет в базу
     *
     * @param approval согласование на отмену.
     */
    void cancel(T approval);

    /**
     * Найти или создать согласование.
     *
     * @param actionId id заявки
     * @return exists approve or new
     */
    T findOrCreate(UUID actionId);

    /**
     * @param actionId id of action
     * @return approval
     */
    Optional<T> findApprovalByActionId(UUID actionId);

    /**
     * Список согласований по ID заявки и статусам
     *
     * @param actionId ID заявки
     * @param statuses статусы
     * @return список согласований
     */
    List<T> findApprovalByActionIdAndStatuses(UUID actionId, Set<Status> statuses);

    /**
     * Создать согласование.
     *
     * @param approval согласование.
     */
    void save(T approval);

    /**
     * Запустить согласование.
     *
     * @param approveId        согласование.
     * @param approvedByUserId кто сделал approve.
     */
    void approve(@NotNull UUID approveId, UUID approvedByUserId);

    /**
     * Запустить согласование.
     *
     * @param actionId actionId.
     */
    void handlingAutoApproving(@NotNull UUID actionId);

    /**
     * Start autoapprove if new/changed employee
     *
     * @param changedEmployee измененный сотрудник
     */
    void handlingAutoApproving(Employee changedEmployee);

    /**
     * Start autoapprove if new/changed position
     *
     * @param changedPosition измененная должность
     */
    void handlingAutoApproving(Position changedPosition);

    /**
     * Start autoapprove if new/changed department
     *
     * @param changedDepartment измененное подразделение
     */
    void handlingAutoApproving(Department changedDepartment);

    /**
     * Отклонить согласование.
     *
     * @param approveId        id согласования.
     * @param reason           причина отклонения.
     * @param declinedByUserId кто отклонил.
     */
    void decline(UUID approveId, String reason, UUID declinedByUserId);

    /**
     * Получить список активных согласований для пользователя
     *
     * @param userId пользователь, который запрашивает список
     * @return список согласований
     */
    Collection<? extends TripApproveDTO> getActiveApprovalsForUser(UUID userId);

    /**
     * Постраничный список активных согласований для пользователя
     *
     * @param userId пользователь, который запрашивает список
     * @param page   параметры страниц
     * @return DTO
     */
    Page<? extends TripApproveDTO> getActiveApprovalsForUser(UUID userId, Pageable page, EmployeeSearchParams params);


    /**
     * Постраничный список неактивных согласований для пользователя
     *
     * @param userId пользователь, который запрашивает список
     * @param page   параметры страниц
     * @return DTO
     */
    Page<? extends TripApproveDTO> getClosedApprovalsForUser(UUID userId, Pageable page);

    /**
     * Получить список неактивных согласований для пользователя
     *
     * @param userId пользователь, который запрашивает список
     * @return список согласований
     */
    Collection<? extends TripApproveDTO> getClosedApprovalsForUser(UUID userId);

    static <A extends Approval> List<A> removeDuplicates(Collection<A> result) {
        return new ArrayList<>(result.stream()
                .collect(Collectors.toMap(Approval::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
                .values());
    }

}
