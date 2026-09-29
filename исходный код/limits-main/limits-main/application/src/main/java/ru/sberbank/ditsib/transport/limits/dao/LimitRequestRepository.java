package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий заявок на лимиты
 */
@Repository
public interface LimitRequestRepository extends JpaRepository<LimitRequest, UUID>,
        JpaSpecificationExecutor<LimitRequest> {
    
    /**
     * Получить заявки, созданные указанным пользователем
     *
     * @param authorId идентификатор автора
     *
     * @return список заявок на лимиты
     */
    List<LimitRequest> findByAuthorId(UUID authorId);
    
    /**
     * Получить заявки с указанным статусом
     *
     * @param status статус
     *
     * @return список заявок на лимиты
     */
    List<LimitRequest> findByStatus(LimitRequestStatus status);
    
    /**
     * Получить заявки, утвержденные указанным пользователем
     *
     * @param approverId идентификатор пользователя
     *
     * @return список заявок на лимиты
     */
    List<LimitRequest> findAllByApproverListEmployeeIdAndApproverListApprovalState(
            UUID approverId, ApprovalState approvalState);
    
    /**
     * Получить заявки, утвержденные указанным пользователем
     *
     * @param approverId идентификатор пользователя
     *
     * @return список заявок на лимиты
     */
    Page<LimitRequest> findAllByApproverListEmployeeIdAndApproverListApprovalStateIn(
            UUID approverId, Set<ApprovalState> approvalStateSet, Pageable pageable);
    
    /**
     * Получить заявки на лимиты по статусу
     *
     * @param status статус заявки
     *
     * @return список заявок на лимиты
     */
    List<LimitRequest> findAllByAuthorOrganizationIdAndStatus(UUID organizationId, LimitRequestStatus status);

    @Query("select new ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO(limitRequest.transportType, count(limitRequest)) " +
            "from LimitRequest limitRequest " +
            "inner join limitRequest.approverList approverList " +
            "inner join approverList.employee approver_employee " +
            "where limitRequest.status in :status " +
            "and approver_employee.id = :approverId " +
            "group by limitRequest.transportType")
    List<GetLimitRequestsStatsV2DTO> getStatsByTransportType(List<LimitRequestStatus> status, UUID approverId);
}
