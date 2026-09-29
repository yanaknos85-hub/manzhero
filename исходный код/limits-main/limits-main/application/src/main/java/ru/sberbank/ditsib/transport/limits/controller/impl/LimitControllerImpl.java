package ru.sberbank.ditsib.transport.limits.controller.impl;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.limits.business.Limits;
import ru.sber.transport.limits.web.providers.DepartmentsProvider;
import ru.sber.transport.limits.web.providers.EmployeeProvider;
import ru.sber.transport.limits.web.providers.SharingsProvider;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryTypeOld;
import ru.sberbank.ditsib.transport.limits.controller.LimitController;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitsStatsController;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitProblemException;
import ru.sberbank.ditsib.transport.limits.mapper.*;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of limit controller service.
 */
@Slf4j
@RestController
@Transactional
@Scope("request")
public class LimitControllerImpl extends BaseController implements LimitController {

    private final LimitService limitService;
    private final DepLimitService depLimitService;
    private final EmployeeService employeeService;
    private final LimitSpendingService limitSpendingService;
    private final LimitTransferHistoryService limitTransferHistoryService;
    private final LimitSpendingMapper limitSpendingMapper;
    private final LimitsStatsController limitsStatsController;
    private final Limits limits;
    private final VersionConverter versionConverter;
    private final FilterWebMapper filterMapping;

    private final EmployeeMapper employeeMapper;

    private final DepartmentMapper departmentMapper;

    private final LimitSharingMapper sharingsMapper;

    private final EmployeeProvider employees;

    private final DepartmentsProvider departments;

    private final SharingsProvider sharings;

    @Value("${spring.application.name}")
    private String appName;

    public LimitControllerImpl(LimitSharingMapper limitSharingMapper, LimitService limitService, DepLimitService depLimitService, EmployeeService employeeService, LimitSpendingService limitSpendingService, LimitTransferHistoryService limitTransferHistoryService, LimitSpendingMapper limitSpendingMapper, LimitsStatsController limitsStatsController, Limits limits, VersionConverter versionConverter, FilterWebMapper filterMapping, EmployeeMapper employeeMapper, DepartmentMapper departmentMapper, LimitSharingMapper sharingsMapper, EmployeeProvider employees, DepartmentsProvider departments, SharingsProvider sharings) {
        super(limitSharingMapper);
        this.limitService = limitService;
        this.depLimitService = depLimitService;
        this.employeeService = employeeService;
        this.limitSpendingService = limitSpendingService;
        this.limitTransferHistoryService = limitTransferHistoryService;
        this.limitSpendingMapper = limitSpendingMapper;
        this.limitsStatsController = limitsStatsController;
        this.limits = limits;
        this.versionConverter = versionConverter;
        this.filterMapping = filterMapping;
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
        this.sharingsMapper = sharingsMapper;
        this.employees = employees;
        this.departments = departments;
        this.sharings = sharings;
    }

    @CheckOrganizationAccess
    @Override
    public List<GetLimitDTO> search(@Organization UUID organizationId,
                                    LimitSearchDTO limitSearchDTO,
                                    JwtAuthenticationToken authentication) {
        return search(limitSearchDTO.getOrganizationId() == null ? organizationId : limitSearchDTO.getOrganizationId(),
                limitSearchDTO,
                authentication,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @CheckOrganizationAccess
    @Override
    public Page<GetLimitDTO> search(@Organization UUID organizationId,
                                    LimitSearchDTO limitSearchDTO,
                                    JwtAuthenticationToken authentication,
                                    Pageable pageable) {
        limitSearchDTO.setOrganizationId(limitSearchDTO.getOrganizationId() == null ? organizationId : limitSearchDTO.getOrganizationId());
        limitSearchDTO.setPage(pageable.getPageNumber());
        limitSearchDTO.setSize(pageable.getPageSize());
        final var sort = pageable.getSort();
        var field = "human_readable_id";
        var direction = Sort.Direction.ASC;
        if (sort.isSorted()) {
            final var sortData = sort.iterator().next();
            field = sortData.getProperty();
            direction = sortData.getDirection();
        }
        final var result = limits.get(filterMapping.toBusiness(limitSearchDTO), pageable.getPageNumber(), pageable.getPageSize(), field, Direction.valueOf(direction.name())).map(versionConverter::toV1);
        final var resultMap = result.getContent().parallelStream().collect(Collectors.toMap(GetLimitDTO::getId, Function.identity()));
        final var futures = new ArrayList<CompletableFuture<Void>>(4);
        try (final var executor = Executors.newFixedThreadPool(4)) {
            final var limitIds = resultMap.values().parallelStream().map(GetLimitDTO::getId).toList();

            futures.add(CompletableFuture.supplyAsync(() -> employees.getOwnerOfLimits(limitIds), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setOwner(employeeMapper.mapToDto(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> employees.getEmployeeOfLimits(resultMap.values().parallelStream().map(GetLimitDTO::getId).filter(Objects::nonNull).toList()), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setEmployee(employeeMapper.mapToDto(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> departments.getOfLimits(limitIds), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setDepartment(departmentMapper.toDto(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> sharings.get(limitIds), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setLimitSharingDTOList(e.getValue().stream().map(sharingsMapper::toDto).toList()))));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        return new PageImpl<>(result.getContent(), pageable, result.getPageData().totalElements());
    }

    @Override
    public List<RemainsPerPeriodDTO> getRemainsPerPeriod(List<UUID> requestList, JwtAuthenticationToken authentication) {
        List<RemainsPerPeriodDTO> remainsList = new ArrayList<>();
        for (UUID requestId : requestList) {
            var limitSpending = limitSpendingService.getByRequestId(requestId);
            if (limitSpending == null) {
                continue;
            }
            var limitSharingPerPeriod = Optional.ofNullable(limitSpending.getLimitSharingPerPeriod()).orElseGet(limitSpending::getLimitSharingPerPeriod);
            var limit = limitSharingPerPeriod.getLimitSharing().getLimit();
            UUID departmentId = null;
            if (limit.getLimitType() == LimitType.DEPARTMENT) {
                departmentId = ((DepLimit) limit).getDepartment().getId();
            }
            if (limit.getLimitType() == LimitType.EMPLOYEE) {
                DepLimit depLimit = (DepLimit) limit.getParent();
                departmentId = depLimit.getDepartment().getId();
            }
            var remainsDTO = new RemainsPerPeriodDTO();
            remainsDTO.setRequestId(requestId);
            remainsDTO.setLimitId(limit.getId());
            remainsDTO.setLimitHumanId(limit.getHumanReadableId());
            remainsDTO.setLimitType(limit.getLimitType().getName());
            remainsDTO.setPeriod(getPeriod(limitSharingPerPeriod.getPeriod()));
            remainsDTO.setBalancePerPeriod(limitSharingPerPeriod.getBalance());
            if (departmentId != null) {
                remainsDTO.setDepartmentId(departmentId);
            }
            remainsList.add(remainsDTO);
        }
        return remainsList;
    }

    // Предполагается впоследствии либо решение с пейджингом либо прекращение использования данного апи
    @CheckOrganizationAccess
    @Override
    public List<LimitSpendingDTO> getSpendings(@Organization UUID organizationId,
                                               UUID limitId,
                                               Integer maxRecords) {
        List<LimitSpending> fullList = limitSpendingService.getByLimit(limitId);
        ArrayList<LimitSpending> list = new ArrayList<>();
        for (int i = 0; i < maxRecords && i < fullList.size(); i++) {
            list.add(fullList.get(i));
        }
        return list.stream().map(limitSpendingMapper::toDto).toList();
    }

    @Override
    public GetLimitDTO getLimitByRequest(UUID requestId,
                                         JwtAuthenticationToken token) {
        LimitSpending limitSpending = limitSpendingService.getByRequestId(requestId);
        if (limitSpending == null) {
            throw new LimitProblemException("Лимит по заявке не найден!");
        }
        return transformLimitEntityToDTO(limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit(), employeeService);
    }

    @CheckOrganizationAccess
    @Override
    public Page<GetLimitHistoryOldDTO> getLimitHistoryOld(@Organization UUID organizationId,
                                                          UUID limitId,
                                                          Integer year,
                                                          Pageable pageable) {
        var fullList1 = limitTransferHistoryService.getByLimit(year, limitId);

        var list1 = fullList1.stream()
                .map(e -> transformTransferHistoryToLimitHistoryDTO(e, limitId)).toList();

        var fullList2 = limitSpendingService.getByLimit(limitId);
        var list2 = fullList2.stream()
                .map(this::transformLimitSpendingToLimitHistoryDTO).toList();
        var listAll = new ArrayList<GetLimitHistoryOldDTO>();
        listAll.addAll(list1);
        listAll.addAll(list2);
        listAll.sort(Comparator.comparing(GetLimitHistoryOldDTO::getCreationTime));
        return createPageFromList(listAll, pageable);
    }

    @CheckOrganizationAccess
    @Override
    public LimitStatsDTO getLimitStats(@Organization UUID organizationId,
                                       Integer year,
                                       Integer month,
                                       Integer day,
                                       UUID departmentId, JwtAuthenticationToken authentication) {
        return versionConverter.toV1(limitsStatsController.getLimitStats(organizationId, year, ru.sberbank.ditsib.transport.limits.dto.v2.Month.values()[month], day, departmentId));
    }

    @CheckOrganizationAccess
    @Override
    public LimitStatsDTO getLimitStats(@Organization UUID organizationId,
                                       Integer year,
                                       UUID departmentId,
                                       JwtAuthenticationToken authentication) {
        return versionConverter.toV1(limitsStatsController.getLimitStats(organizationId, year, null, null, departmentId));
    }

    @Override
    public GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            @RequestBody @Valid GeneralAnalyticalReportRequestDTO request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication) {
        return limitsStatsController.getGeneralAnalyticalReportData(versionConverter.toV2(request), authentication);
    }

    @Override
    public List<GetLimitDTO> getLimitChildren(UUID limitId,
                                              String limitTypeOpt,
                                              JwtAuthenticationToken token) {
        var filter = new LimitSearchDTO();
        filter.setParentLimitId(limitId);
        filter.setLimitType(Optional.ofNullable(limitTypeOpt).map(LimitType::valueOf).orElse(null));
        filter.setPage(0);
        filter.setSize(Integer.MAX_VALUE);
        final var result = limits.get(filterMapping.toBusiness(filter), 0, Integer.MAX_VALUE, "human_readable_id", Direction.ASC).map(versionConverter::toV1).getContent();
        if (!result.isEmpty()) {
            final var resultMap = result.parallelStream().collect(Collectors.toMap(GetLimitDTO::getId, Function.identity()));
            final var futures = new ArrayList<CompletableFuture<Void>>(4);
            try (final var executor = Executors.newFixedThreadPool(4)) {
                final var limitIds = resultMap.values().parallelStream().map(GetLimitDTO::getId).toList();

                futures.add(CompletableFuture.supplyAsync(() -> employees.getOwnerOfLimits(limitIds), executor)
                        .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setOwner(employeeMapper.mapToDto(e.getValue())))));
                futures.add(CompletableFuture.supplyAsync(() -> employees.getEmployeeOfLimits(resultMap.values().parallelStream().map(GetLimitDTO::getId).filter(Objects::nonNull).toList()), executor)
                        .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setEmployee(employeeMapper.mapToDto(e.getValue())))));
                futures.add(CompletableFuture.supplyAsync(() -> departments.getOfLimits(limitIds), executor)
                        .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setDepartment(departmentMapper.toDto(e.getValue())))));
                futures.add(CompletableFuture.supplyAsync(() -> sharings.get(limitIds), executor)
                        .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setLimitSharingDTOList(e.getValue().stream().map(sharingsMapper::toDto).toList()))));
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
        return result;
    }

    static <T> Page<T> createPageFromList(List<T> list, Pageable pageable) {
        if (list == null) {
            throw new IllegalArgumentException("To create a Page, the list mustn't be null!");
        }

        int startOfPage = pageable.getPageNumber() * pageable.getPageSize();
        if (startOfPage > list.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        int endOfPage = Math.min(startOfPage + pageable.getPageSize(), list.size());
        return new PageImpl<>(list.subList(startOfPage, endOfPage), pageable, list.size());
    }

    /**
     * Transform entity to dto.
     *
     * @param limitSpending limitSpending.
     * @return dto.
     */
    private GetLimitHistoryOldDTO transformLimitSpendingToLimitHistoryDTO(LimitSpending limitSpending) {
        GetLimitHistoryOldDTO getLimitHistoryOldDTO = new GetLimitHistoryOldDTO();
        getLimitHistoryOldDTO.setAuthor(limitSpending.getEmployee().getId());
        getLimitHistoryOldDTO.setCreationTime(limitSpending.getReservationTime());
        getLimitHistoryOldDTO.setSum(limitSpending.getSumReserved());
        getLimitHistoryOldDTO.setYear(limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getYear());
        getLimitHistoryOldDTO.setPeriod(limitSpending.getLimitSharingPerPeriod().getPeriod().ordinal());
        getLimitHistoryOldDTO.setLimitId(limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId());
        getLimitHistoryOldDTO.setTransportType(limitSpending.getLimitSharingPerPeriod().getLimitSharing().getTransportType());
        getLimitHistoryOldDTO.setHistoryType(LimitHistoryTypeOld.OUTCOME);
        return getLimitHistoryOldDTO;
    }

    /**
     * Transform entity to dto.
     *
     * @param limitTransferHistory limitTransferHistory.
     * @return dto.
     */
    private GetLimitHistoryOldDTO transformTransferHistoryToLimitHistoryDTO(LimitTransferHistory limitTransferHistory,
                                                                            UUID limitId) {
        GetLimitHistoryOldDTO getLimitHistoryOldDTO = new GetLimitHistoryOldDTO();
        getLimitHistoryOldDTO.setAuthor(limitTransferHistory.getAuthor());
        getLimitHistoryOldDTO.setCreationTime(limitTransferHistory.getCreationTime());
        getLimitHistoryOldDTO.setSum(limitTransferHistory.getSum());
        getLimitHistoryOldDTO.setYear(limitTransferHistory.getYear());

        if (limitTransferHistory.getSourceLimitId().equals(limitId)) {
            getLimitHistoryOldDTO.setLimitId(limitTransferHistory.getSourceLimitId());
            getLimitHistoryOldDTO.setTransportType(limitTransferHistory.getSourceTransportType());
            getLimitHistoryOldDTO.setPeriod(limitTransferHistory.getSourcePeriod().ordinal());
            getLimitHistoryOldDTO.setHistoryType(LimitHistoryTypeOld.OUTCOME);
        }
        if (limitTransferHistory.getTargetLimitId().equals(limitId)) {
            getLimitHistoryOldDTO.setLimitId(limitTransferHistory.getTargetLimitId());
            getLimitHistoryOldDTO.setTransportType(limitTransferHistory.getTargetTransportType());
            getLimitHistoryOldDTO.setPeriod(limitTransferHistory.getTargetPeriod().ordinal());
            getLimitHistoryOldDTO.setHistoryType(LimitHistoryTypeOld.INCOME);
        }
        return getLimitHistoryOldDTO;
    }

    @CheckOrganizationAccess
    @Override
    public Boolean deleteLimits(@Organization UUID organizationId,
                                JwtAuthenticationToken authentication) {
        Employee author = getEmployee(authentication);
        return depLimitService.deleteLimits(organizationId, author) > 0;
    }

    @CheckOrganizationAccess
    @Override
    public Boolean deleteLimitsByServiceTypeAndYear(@Organization UUID organizationId,
                                                    String serviceType,
                                                    Integer year,
                                                    JwtAuthenticationToken authentication) {
        Employee author = getEmployee(authentication);
        return depLimitService.deleteLimits(organizationId, serviceType, year, author) > 0;
    }

    @CheckOrganizationAccess
    @Override
    public List<String> auditLimits(@Organization UUID organizationId,
                                    Integer year,
                                    JwtAuthenticationToken authentication) {
        return depLimitService.auditLimits(organizationId, year, true);
    }

    @CheckOrganizationAccess
    @Override
    public TaskStartedDto auditLimitsAsync(@Organization UUID organizationId,
                                           Integer year,
                                           JwtAuthenticationToken authentication) {
        return depLimitService.auditLimitsAsync(organizationId, year, true);
    }

    @CheckOrganizationAccess
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TaskStartedDto closeLimitsAsync(@Organization UUID organizationId,
                                           UUID limitId,
                                           JwtAuthenticationToken authentication, String source) {
        return depLimitService.closeLimitsAsync(organizationId, limitId, authentication, appName, source);
    }

    @Override
    public GetLimitInfoDto getLimitInfoByDate(UUID employeeId, LocalDate date, TransportTypeEnum transportType) {
        var employee = employeeService.get(employeeId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        return limitService.getLimiInfoByDate(employee, date, transportType);
    }

    private Employee getEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeService.getByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));
    }

    private int getPeriod(@NonNull Object period) {
        if (period instanceof Period month) {
            return month.ordinal();
        } else {
            return (Integer) period;
        }
    }
}