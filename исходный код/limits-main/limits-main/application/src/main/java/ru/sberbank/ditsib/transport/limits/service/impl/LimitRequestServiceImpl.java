package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.ApproverRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitRequestRepository;
import ru.sberbank.ditsib.transport.limits.dto.EmployeeState;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestApproveDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestCancelDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee_;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus.CANCELLED;

/**
 * Implementation of service for working with personal requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LimitRequestServiceImpl implements LimitRequestService {

    private final LimitRequestRepository limitRequestRepository;

    private final ApproverRepository approverRepository;

    private final DepartmentService departmentService;

    private final OrganizationService organizationService;

    private final EmpLimitService empLimitService;

    private final DepLimitService depLimitService;

    private final LimitService limitService;

    private final LimitSharingService limitSharingService;

    @Qualifier("sQGeneratorLimits")
    private final SQGenerator sqGenerator;

    @Override
    public LimitRequest add(final LimitRequest limitRequest) {
        limitRequest.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        var organization =
                departmentService.get(limitRequest.getAuthor().getDepartmentId()).map(Department::getOrganizationId)
                                 .flatMap(organizationService::get).orElseThrow(() -> new EntityNotFoundException(Department.class,
                                                                                                                  limitRequest.getAuthor().getDepartmentId()));

        LimitRequest result;
        try {
            var limitOpt = limitService.getUpperLevelActiveLimitNoChildren(organization.getId(),
                                                                 LimitServiceType.getLimitServiceTypeByTransportType(limitRequest.getTransportType()).name(),
                                                                 limitRequest.getYear());
            if (limitOpt.isEmpty()) {
                throw new LimitLogicException("LimitRequestService: add: верхнеуровневый лимин не найден!");
            }
            if (limitOpt.get().getLimitSharingType() == LimitSharingType.QUARTER) {
                if (limitRequest.getPeriod().ordinal() > 3) {
                    throw new LimitLogicException("Период в заявке превышает максимальное значение 3");
                }
            } else {
                if (limitRequest.getPeriod().ordinal() > 11) {
                    throw new LimitLogicException("Период в заявке превышает максимальное значение 11");
                }
            }
            String humanReadableId = sqGenerator.getNextId(Prefix.OL, organization.getDigitId());
            limitRequest.setHumanReadableId(humanReadableId);
            result = limitRequestRepository.save(limitRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }

    @Override
    public void save(LimitRequest limitRequest) {
        try {
            limitRequestRepository.save(limitRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void delete(LimitRequest limitRequest) {
        try {
            limitRequestRepository.delete(limitRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public LimitRequest cancel(LimitRequestCancelDTO dto) {
        var limitRequest = get(dto.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, dto.getRequestId()));
        limitRequest.setStatus(LimitRequestStatus.CANCELLED);
        limitRequest.setStatusCode(LimitRequestStatus.CANCELLED_BY_USER);
        limitRequest.setDeclineReason(dto.getDescription());
        save(limitRequest);
        return limitRequest;
    }

    @Override
    public LimitRequest cancel(UUID requestId, String reason) {
        var limitRequest = get(requestId)
                .orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, requestId));
        limitRequest.setStatus(LimitRequestStatus.CANCELLED);
        limitRequest.setStatusCode(LimitRequestStatus.CANCELLED_BY_USER);
        limitRequest.setDeclineReason(reason);
        save(limitRequest);
        return limitRequest;
    }

    @Override
    public void cancel(LimitRequest request, LimitRequestStatus.LimitStatusCode statusCode) {
        request.setStatus(CANCELLED);
        request.setStatusCode(statusCode.getCode());
        request.setDeclineReason(statusCode.getDescription());
        save(request);
    }

    @Override
    public LimitRequest approve(LimitRequestApproveDTO dto, LimitRequest limitRequest, Employee author) {
        try {
            var approver = limitRequest.getApproverList().stream()
                                            .filter(e -> e.getEmployee().getId().equals(author.getId()))
                                            .findAny().orElseThrow(() -> new LimitLogicException("Залогиненный пользователь не является апрувером по заявке!"));
            // if status is decline - go away
            if (ApprovalState.DECLINED.equals(dto.getApprovalState())) {
                approver.setApprovalState(ApprovalState.DECLINED);
                approverRepository.save(approver);
                return get(limitRequest.getId()).orElse(null);
            }
            var currentYear = LocalDate.now(ZoneOffset.UTC).getYear();
            checkYear(limitRequest, currentYear);
            checkApprovalState(dto.getApprovalState());
            // if status == comfirm and sum = 0 throw exception
            final var sum = dto.getSum();
            checkSum(dto, sum);
            // status confirmed from here
            // check sum not too big
            final var sumPaid = limitRequest.getApproverList().stream()
                                       .filter(e -> e.getApprovalState().equals(ApprovalState.APPROVED))
                                       .map(Approver::getSum).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            final var restSumToGet = limitRequest.getSum().subtract(sumPaid);
            checkApprovedSum(sum, restSumToGet);

            // достать целевой и источник лимит по признакам
            Limit targetLimit = null;
            Limit sourceLimit = null;
            if (limitRequest.getLimitType() == LimitType.EMPLOYEE) {
                final var limitServiceType = LimitServiceType.getLimitServiceTypeByTransportType(limitRequest.getTransportType()).name();
                sourceLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(limitRequest.getAuthor().getDepartmentId(),
                                                                                        limitRequest.getYear(),
                                                                                        limitServiceType);
                if (sourceLimit == null) {
                    throw new LimitLogicException("Лимит для подразделения сотрудника и указанного года не найден");
                }
                targetLimit = empLimitService.getByEmployeeAndYearAndLimitServiceType(limitRequest.getAuthor(),
                                                                                      limitRequest.getYear(),
                                                                                      limitServiceType);
                if (targetLimit == null) {
                    targetLimit = empLimitService.getEmpLimit(limitRequest.getAuthor(), author, (DepLimit) sourceLimit);
                }
                limitSharingService.getLimitSharing(targetLimit, limitRequest.getTransportType(), author);
            }
            if (limitRequest.getLimitType() == LimitType.DEPARTMENT) {
                Department department = departmentService.get(limitRequest.getAuthor().getDepartmentId()).orElse(null);
                if (department == null) {
                    throw new LimitLogicException("Одобрение: подразделение не найдено");
                }
                targetLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(),
                                                                                        limitRequest.getYear(),
                                                                                        limitRequest.getTransportType().getServiceType()
                                                                                                    .equals(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                                                                        ? "PASSENGER"
                                                                                        : "CARGO");
                if (targetLimit == null) {
                    throw new LimitLogicException("Одобрение: лимит целевого подразделения отсутствует");
                }
                if (targetLimit.getParent() == null) {
                    throw new LimitLogicException("Одобрение: лимит верхнеуровневого подразделения не может быть пополнен");
                }
                if (!LimitStatus.SHARED.equals(targetLimit.getLimitStatus())) {
                    throw new LimitLogicException("Целевой лимит не в статусе распределен");
                }
                sourceLimit = targetLimit.getParent();
            }
            checkLimit(sourceLimit);
            // сделать перенос денег одноразово
            var source = new LimitData(
                    sourceLimit,
                    limitRequest.getTransportType(),
                    Period.create(limitRequest.getPeriod().name())
            );
            var target = new LimitData(
                    targetLimit,
                    limitRequest.getTransportType(),
                    Period.create(limitRequest.getPeriod().name())
            );
            limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL, false);
            approver.setSum(sum);
            approver.setApprovalState(ApprovalState.APPROVED);
            approver.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            approverRepository.save(approver);
            return get(limitRequest.getId()).orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public LimitRequest approve(LimitRequestApproveV2DTO dto, LimitRequest limitRequest, Employee author) {
        try {
            Approver approver = limitRequest.getApproverList().stream()
                .filter(e -> e.getEmployee().getId().equals(author.getId()))
                .findAny().orElse(null);
            // if status is decline - go away
            if (dto.approvalState() == ApprovalState.DECLINED && approver != null) {
                approver.setApprovalState(ApprovalState.DECLINED);
                approverRepository.save(approver);
                return get(limitRequest.getId()).orElse(null);
            }
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            checkYear(limitRequest, currentYear);
            if (approver == null) {
                throw new LimitLogicException("Залогиненный пользователь не является апрувером по заявке!");
            }
            checkApprovalState(dto.approvalState());
            // if status == comfirm and sum = 0 throw exception
            final var sum = dto.sum();
            if (ApprovalState.APPROVED == dto.approvalState() && sum.compareTo(BigDecimal.ZERO) == 0) {
                throw new LimitLogicException("Подтверждение нулевой суммой запрещено");
            }
            // status confirmed from here
            // check sum not too big
            final var sumPaid = limitRequest.getApproverList().stream()
                                       .filter(e -> e.getApprovalState().equals(ApprovalState.APPROVED))
                                       .map(Approver::getSum)
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            final var restSumToGet = limitRequest.getSum().subtract(sumPaid);
            checkApprovedSum(sum, restSumToGet);

            // достать целевой и источник лимит по признакам
            Limit targetLimit = null;
            Limit sourceLimit = null;
            if (limitRequest.getLimitType() == LimitType.EMPLOYEE) {
                final var limitServiceType = LimitServiceType.getLimitServiceTypeByTransportType(limitRequest.getTransportType()).name();
                sourceLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(limitRequest.getAuthor().getDepartmentId(),
                                                                                        limitRequest.getYear(),
                                                                                        limitServiceType);
                if (sourceLimit == null) {
                    throw new LimitLogicException("Лимит для подразделения сотрудника и указанного года не найден");
                }
                targetLimit = empLimitService.getByEmployeeAndYearAndLimitServiceType(limitRequest.getAuthor(),
                                                                                      limitRequest.getYear(),
                                                                                      limitServiceType);
                if (targetLimit == null) {
                    targetLimit = empLimitService.getEmpLimit(limitRequest.getAuthor(), author, (DepLimit) sourceLimit);
                }
                limitSharingService.getLimitSharing(targetLimit, limitRequest.getTransportType(), author);
            }
            if (limitRequest.getLimitType() == LimitType.DEPARTMENT) {
                Department department = departmentService.get(limitRequest.getAuthor().getDepartmentId()).orElse(null);
                if (department == null) {
                    throw new LimitLogicException("Одобрение: подразделение не найдено");
                }
                targetLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(),
                                                                                        limitRequest.getYear(),
                                                                                        limitRequest.getTransportType().getServiceType()
                                                                                                    .equals(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                                                                        ? "PASSENGER"
                                                                                        : "CARGO");
                if (targetLimit == null) {
                    throw new LimitLogicException("Одобрение: лимит целевого подразделения отсутствует");
                }
                if (targetLimit.getParent() == null) {
                    throw new LimitLogicException("Одобрение: лимит верхнеуровневого подразделения не может быть пополнен");
                }
                if (!LimitStatus.SHARED.equals(targetLimit.getLimitStatus())) {
                    throw new LimitLogicException("Целевой лимит не в статусе распределен");
                }
                sourceLimit = targetLimit.getParent();
            }
            checkLimit(sourceLimit);
            // сделать перенос денег одноразово
            var source = new LimitData(
                    sourceLimit,
                    limitRequest.getTransportType(),
                    Period.create(limitRequest.getPeriod().name())
            );
            var target = new LimitData(
                    targetLimit,
                    limitRequest.getTransportType(),
                    Period.create(limitRequest.getPeriod().name())
            );
            limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL, false);
            approver.setSum(sum);
            approver.setApprovalState(ApprovalState.APPROVED);
            approver.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            approverRepository.save(approver);
            return get(limitRequest.getId()).orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<LimitRequest> get(UUID id) {
        return limitRequestRepository.findById(id);
    }

    @Override
    public List<LimitRequest> getAll() {
        return limitRequestRepository.findAll();
    }

    @Override
    public List<LimitRequest> getByStatus(LimitRequestStatus status) {
        return limitRequestRepository.findByStatus(status);
    }

    @Override
    public List<LimitRequest> getByOrganizationIdAndStatus(UUID organizationId,
                                                           LimitRequestStatus status) {
        return limitRequestRepository.findAllByAuthorOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public List<LimitRequest> getByAuthor(@NotNull UUID authorId) {
        return limitRequestRepository.findByAuthorId(authorId);
    }

    @Override
    public List<LimitRequest> getByApprover(@NotNull UUID approverId) {
        return limitRequestRepository.findAllByApproverListEmployeeIdAndApproverListApprovalState(
                approverId, ApprovalState.AWAITING_APPROVAL);
    }

    @Override
    public Page<LimitRequest> getActiveByApprover(@NotNull UUID approverId, Pageable pageable) {
        Set<ApprovalState> stateSet = new HashSet<>();
        stateSet.add(ApprovalState.AWAITING_APPROVAL);
        return limitRequestRepository.findAllByApproverListEmployeeIdAndApproverListApprovalStateIn(
                approverId, stateSet, pageable);
    }

    @Override
    public Page<LimitRequest> getOldByApprover(@NotNull UUID approverId, Pageable pageable) {
        Set<ApprovalState> stateSet = new HashSet<>();
        stateSet.add(ApprovalState.APPROVED);
        stateSet.add(ApprovalState.DECLINED);
        return limitRequestRepository.findAllByApproverListEmployeeIdAndApproverListApprovalStateIn(
                approverId, stateSet, pageable);
    }

    @Override
    public LimitRequest changeStatus(LimitRequest toChange, LimitRequestStatus newStatus, Employee activeUser) {
        toChange.setStatus(newStatus);
        return limitRequestRepository.save(toChange);
    }

    @Override
    public Page<LimitRequest> getAll(LimitRequestStatus status, ApprovalState approvalState, Boolean active,
                                     TransportTypeEnum transportType, EmployeeState myState, int page, int size, Employee employee) {
        var pageRequest = PageRequest.of(page, size, Sort.by(LimitRequest_.CREATION_TIME));
        return limitRequestRepository.findAll((root, query, cb) -> {
            var predicate = cb.isTrue(cb.literal(true));
            predicate = append(predicate, cb, root.get(LimitRequest_.STATUS), false, status);
            if (approvalState != null) {
                predicate = switch (approvalState) {
                    case APPROVED -> append(predicate, cb, root.get(LimitRequest_.STATUS), false, LimitRequestStatus.DONE_FULLY, LimitRequestStatus.DONE_PARTLY);
                    case DECLINED -> append(predicate, cb, root.get(LimitRequest_.STATUS), false, LimitRequestStatus.CANCELLED_BY_USER, LimitRequestStatus.CANCELLED_BY_DEADLINE, LimitRequestStatus.CANCELLED_BY_APPROVERS);
                    case AWAITING_APPROVAL -> append(predicate, cb, root.get(LimitRequest_.STATUS), false, LimitRequestStatus.INIT);
                };
            }
            if (myState != null) {
                predicate = switch (myState) {
                    case AUTHOR -> append(predicate, cb, root.get(LimitRequest_.author).get(Employee_.ID), false, employee.getId());
                    case APPROVER -> appendApprovers(employee, root, cb, predicate);
                };
            }
            if (active != null) {
                predicate = append(predicate, cb, root.get(LimitRequest_.STATUS), !active, LimitRequestStatus.INIT);
            }
            if (transportType != null) {
                predicate = append(predicate, cb, root.get(LimitRequest_.TRANSPORT_TYPE), false, transportType);
            }
            return predicate;
        }, pageRequest);
    }

    @Override
    public List<GetLimitRequestsStatsV2DTO> getStatistic(boolean active, Employee employee) {
        if (active) {
            return limitRequestRepository.getStatsByTransportType(List.of(LimitRequestStatus.INIT), employee.getId());
        }

        return limitRequestRepository.getStatsByTransportType(List.of(LimitRequestStatus.DONE_FULLY,
                LimitRequestStatus.DONE_PARTLY, LimitRequestStatus.CANCELLED), employee.getId());
    }

    private Predicate appendApprovers(Employee employee, Root<LimitRequest> root, CriteriaBuilder cb, Predicate predicate) {
        var approvers = root.join(LimitRequest_.approverList);
        var employeeApprover = approvers.join(Approver_.employee);
        return append(predicate, cb, employeeApprover.get(Employee_.ID), false, employee.getId());
    }

    private Predicate append(Predicate predicate, CriteriaBuilder cb, Path<Object> path, boolean reverse, Object... values) {
        if (values.length > 0) {
            var predicates = new LinkedList<Predicate>();
            for (var value : values) {
                if (value != null) {
                    var curPredicate = cb.equal(path, value);
                    if (reverse) {
                        curPredicate = cb.not(curPredicate);
                    }
                    predicates.add(curPredicate);
                }
            }
            predicate = finishPredicate(predicate, cb, predicates);
        }
        return predicate;
    }

    private Predicate finishPredicate(Predicate predicate, CriteriaBuilder cb, LinkedList<Predicate> predicates) {
        if (predicates.isEmpty()) {
            return predicate;
        }
        if (predicates.size() == 1) {
            return cb.and(predicate, predicates.getFirst());
        } else {
            return cb.and(predicate, cb.or(predicates.toArray(Predicate[]::new)));
        }
    }

    private static void checkLimit(Limit sourceLimit) {
        if (!LimitStatus.SHARED.equals(Optional.ofNullable(sourceLimit).map(Limit::getLimitStatus).orElse(null))) {
            throw new LimitLogicException("Исходный лимит не в статусе распределен");
        }
    }

    private static void checkApprovedSum(BigDecimal sum, BigDecimal restSumToGet) {
        if (sum.compareTo(restSumToGet) > 0) {
            throw new LimitLogicException("Подтвержденная сумма выше необходимого");
        }
    }

    private static void checkSum(LimitRequestApproveDTO dto, BigDecimal sum) {
        if (ApprovalState.APPROVED.equals(dto.getApprovalState()) && sum.compareTo(BigDecimal.ZERO) == 0L) {
            throw new LimitLogicException("Подтверждение нулевой суммой запрещено");
        }
    }

    private static void checkApprovalState(ApprovalState state) {
        if (ApprovalState.AWAITING_APPROVAL.equals(state)) {
            throw new LimitLogicException("На согласование передан небинарный статус");
        }
    }

    private static void checkYear(LimitRequest limitRequest, int currentYear) {
        if (limitRequest.getYear() < currentYear) {
            throw new LimitLogicException("Одобрение заявки: год раньше текущего");
        }
    }
}
