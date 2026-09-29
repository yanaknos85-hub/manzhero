package ru.sberbank.ditsib.transport.limits.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.EmployeeState;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestApproveDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestCancelDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service of request processing
 */
public interface LimitRequestService {

    /**
     * Сохранить заявку на лимит.
     *
     * @param request заявка.
     *
     * @return заявка на лимит
     */
    LimitRequest add(LimitRequest request);

    /**
     * Сохранить заявку на лимит.
     *
     * @param limitRequest заявка.
     */
    void save(LimitRequest limitRequest);

    /**
     * Удалить заявку на лимит.
     *
     * @param request заявка.
     */
    void delete(LimitRequest request);

    /**
     * Отклонить заявку на лимит.
     *
     * @param dto object wth data.
     * @return canceled limit request.
     */
    LimitRequest cancel(LimitRequestCancelDTO dto);

    /**
     * Отклонить заявку на лимит.
     *
     * @param requestId id of request to decline.
     * @param reason reason of decline.
     * @return canceled limit request.
     */
    LimitRequest cancel(UUID requestId, String reason);

    /**
     * Отклонить заявку на лимит.
     *
     * @param request заявка на лимит
     * @param statusCode код статус
     */
    void cancel(LimitRequest request, LimitRequestStatus.LimitStatusCode statusCode);

    /**
     * Утвердить заявку на лимит.
     *
     * @param dto object with data.
     * @param limitRequest limit request.
     * @param author author of request.
     *
     * @return заявка на лимит
     */
    LimitRequest approve(LimitRequestApproveDTO dto, LimitRequest limitRequest, Employee author);

    /**
     * Утвердить заявку на лимит.
     *
     * @param dto object with data.
     * @param limitRequest limit request.
     * @param author author of request.
     *
     * @return заявка на лимит
     */
    LimitRequest approve(LimitRequestApproveV2DTO dto, LimitRequest limitRequest, Employee author);

    /**
     * Получить заявку на лимит по идентификатору.
     *
     * @param id идентификатор заявки.
     *
     * @return заявка.
     */
    Optional<LimitRequest> get(UUID id);

    /**
     * Получить список всех заявок на лимиты.
     *
     * @return список заявок на лимиты.
     */
    List<LimitRequest> getAll();

    /**
     * Получить писок заявок на лимиты, созданных указанным сотрудником
     *
     * @param authorId идентификатор сотрудника
     *
     * @return список заявок на лимиты.
     */
    List<LimitRequest> getByAuthor(@NotNull UUID authorId);

    /**
     * Получить список заявок на лимиты по статусу.
     *
     * @param status статус.
     *
     * @return список заявок на лимиты.
     */
    List<LimitRequest> getByStatus(LimitRequestStatus status);

    /**
     * Получить список заявок на лимиты по идентификатору организации и статусу
     *
     * @param organizationId идентификатор организации
     *
     * @param status статус
     *
     * @return список заявок на лимиты.
     */
    List<LimitRequest> getByOrganizationIdAndStatus(UUID organizationId, LimitRequestStatus status);

    /**
     * Получить список заявок на лимиты для утверждения указанным пользователем
     *
     * @param approverId идентификатор сотрудника
     *
     * @return список заявок на лимиты.
     */
    List<LimitRequest> getByApprover(@NotNull UUID approverId);

    /**
     * Получить список заявок на лимиты для утверждения указанным пользователем
     *
     * @param approverId идентификатор сотрудника
     *
     * @return список заявок на лимиты.
     */
    Page<LimitRequest> getActiveByApprover(@NotNull UUID approverId,
                                                     @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable);

    /**
     * Получить список старых заявок на лимиты, утвержденных или отклоненных указанным сотрудником
     *
     * @param approverId идентификатор сотрудника
     *
     * @return список заявок на лимиты.
     */
    Page<LimitRequest> getOldByApprover(@NotNull UUID approverId,
                                                  @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable);

    /**
     * Изменить статус заявки на лимит
     *
     * @param toChange заявка
     * @param newStatus новый статус
     * @param activeUser сотрудник
     */
    LimitRequest changeStatus(LimitRequest toChange, LimitRequestStatus newStatus, Employee activeUser);

    Page<LimitRequest> getAll(LimitRequestStatus status, ApprovalState approvalState, Boolean active,
                              TransportTypeEnum transportType, EmployeeState myState, int page, int size, Employee employee);

    /**
     * Получение статистики количества заявок по типу транспорта.
     *
     * @param active   признак активности заявок.
     * @param employee сотрудник.
     * @return статистика.
     */
    List<GetLimitRequestsStatsV2DTO> getStatistic(boolean active, Employee employee);
}
