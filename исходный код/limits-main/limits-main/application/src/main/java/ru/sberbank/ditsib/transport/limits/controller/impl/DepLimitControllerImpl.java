package ru.sberbank.ditsib.transport.limits.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.limits.web.api.ManagingApiDelegate;
import ru.sber.transport.limits.web.model.PatchRequestInner;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.controller.DepLimitController;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.dto.v3.SecondarySharingData;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;

/**
 * Implementation of limit controller service.
 */
@RestController
@Slf4j
@Transactional
@Scope("request")
public class DepLimitControllerImpl extends BaseController implements DepLimitController {

    private static final String CANNOT_SHARE_PREV_YEAR_ERROR = "Лимит за прошедшие годы не может быть перераспределен";

    private static final String CANNOT_USE_MY_LIMIT_ERROR =
            "Ошибка: галочку 'Использовать лимит моего подразделения' установить нельзя: есть дочерние лимиты";

    private final LimitService limitService;

    private final DepLimitService depLimitService;

    private final DepartmentService departmentService;

    private final EmployeeService employeeService;

    private final LimitSharingService limitSharingService;

    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    private final ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitController controller;

    private final VersionConverter versionConverter;

    private final ManagingApiDelegate managingApiDelegate;

    @SuppressWarnings("java:S107")
    public DepLimitControllerImpl(LimitSharingMapper limitSharingMapper,
                                  LimitService limitService,
                                  DepLimitService depLimitService,
                                  DepartmentService departmentService,
                                  EmployeeService employeeService,
                                  LimitSharingService limitSharingService,
                                  Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices,
                                  ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitController controller,
                                  VersionConverter versionConverter,
                                  ManagingApiDelegate managingApiDelegate) {
        super(limitSharingMapper);
        this.limitService = limitService;
        this.depLimitService = depLimitService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.limitSharingService = limitSharingService;
        this.limitSharingPerPeriodServices = limitSharingPerPeriodServices;
        this.controller = controller;
        this.versionConverter = versionConverter;
        this.managingApiDelegate = managingApiDelegate;
    }

    @CheckOrganizationAccess
    @Override
    public GetLimitDTO add(
            @ru.sber.transport.authorization.annotations.Organization UUID organizationId,
            DepLimitPrimaryDTO depLimitPrimaryDTO,
            JwtAuthenticationToken authentication
    ) {
        return versionConverter.toV1(controller.add(organizationId, versionConverter.toV2(depLimitPrimaryDTO), authentication, null));
    }

    @Override
    public void edit(UUID limitId, DepLimitPrimaryDTO depLimitPrimaryDTO, JwtAuthenticationToken authentication, String source) {
        var depLimit = depLimitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        if (depLimit.getParent() != null) {
            throw new LimitLogicException("Только верхнеуровневый лимит может быть редактирован");
        }
        if (depLimit.getLimitStatus() != LimitStatus.PLANNING) {
            throw new LimitLogicException("Только верхнеуровневый лимит в статусе ПЛАНИРУЕТСЯ может быть редактирован");
        }
        depLimit.setYear(depLimitPrimaryDTO.getYear());
        var newSum = depLimitPrimaryDTO.getSum();
        var oldSum = depLimit.getSum();
        if (newSum.doubleValue() < oldSum.doubleValue()) {
            throw new LimitLogicException("Ошибка: новая сумма меньше старой!");
        } else {
            var delta = newSum.subtract(oldSum).abs();
            depLimit.setSum(depLimit.getSum().add(delta));
            var reserve = depLimit.getReserve();
            depLimit.setReserve(reserve == null ? delta : reserve.add(delta));
        }
        depLimit.setLimitSharingType(depLimitPrimaryDTO.getLimitSharingType());
        if (depLimitPrimaryDTO.getFinalSharing() != null) {
            if (Boolean.TRUE.equals(depLimitPrimaryDTO.getFinalSharing()) && !depLimit.getChildren().isEmpty()) {
                throw new LimitLogicException("Ошибка: галочку 'Конечное распределение' установить нельзя: есть дочерние лимиты");
            }
            depLimit.setFinalSharing(depLimitPrimaryDTO.getFinalSharing());
        }
        if (depLimitPrimaryDTO.getUseThisLimit() != null) {
            if (Boolean.TRUE.equals(depLimitPrimaryDTO.getUseThisLimit()) && !depLimit.getChildren().isEmpty()) {
                throw new LimitLogicException(CANNOT_USE_MY_LIMIT_ERROR);
            }
            depLimit.setUseThisLimit(depLimitPrimaryDTO.getUseThisLimit());
        }
        depLimitService.save(depLimit);
    }

    @Override
    public void editStatusOrFlags(
            UUID limitId, DepLimitEditDTO depLimitEditDTO, JwtAuthenticationToken authentication, String source
    ) throws JsonProcessingException {
        var objectMapper = getObjectMapper();
        var map = objectMapper.readValue(
                        objectMapper.writeValueAsString(depLimitEditDTO),
                        new TypeReference<Map<String, Object>>() {
                        }
                )
                .entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    final var path = entry.getKey();
                    final var value = new PatchRequestInner(PatchRequestInner.OpEnum.REPLACE,"/" + (path.contains("limit") ? path.replaceAll("limit(\\w)(\\w+)", "$1").toLowerCase() + path.replaceAll("limit(\\w)(\\w+)", "$2") : path));
                    value.setValue(entry.getValue());
                    return value;
                })
                .toList();
        managingApiDelegate.patch(map, limitId);
    }

    @Override
    public void editUseThisLimitFlag(
            UUID limitId, Boolean useThisLimit, JwtAuthenticationToken authentication, String source
    ) {
        var patch = new PatchRequestInner(PatchRequestInner.OpEnum.REPLACE, "/useThisLimit");
        patch.setValue(useThisLimit);
        managingApiDelegate.patch(List.of(patch), limitId);
    }

    @Override
    public GetLimitDTO get(UUID limitId) {
        var depLimit = depLimitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        return transformLimitEntityToDTO(depLimit, employeeService);
    }

    @Override
    public List<GetLimitDTO> getAll() {
        var list = depLimitService.getAll();
        return list.stream().map(depLimit -> transformLimitEntityToDTO(depLimit, employeeService)).toList();
    }

    @Override
    public List<GetLimitDTO> getByDepartment(UUID departmentId) {
        var department = departmentService.get(departmentId).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));
        var list = depLimitService.findAccessible(department.getId());

        return list.stream()
                .map(depLimit -> transformLimitEntityToDTO(depLimit, depLimit.getDepartment(), employeeService))
                .toList();
    }

    @Override
    public GetLimitDTO getByDepartmentAndYear(UUID departmentId, Integer year) {
        var department = departmentService.get(departmentId).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));

        return transformLimitEntityToDTO(depLimitService.getByDepartmentAndYear(department.getId(), year), employeeService);
    }

    @Override
    public List<GetLimitDTO> getByDepartmentAndYearAll(UUID departmentId, Integer year) {
        var department = departmentService.get(departmentId).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));
        List<DepLimit> list = depLimitService.getByDepartmentAndYearAll(department.getId(), year);
        return list.stream().map(depLimit -> transformLimitEntityToDTO(depLimit, employeeService)).toList();
    }

    @Override
    public GetLimitDTO getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType) {
        var department = departmentService.get(departmentId).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));

        return transformLimitEntityToDTO(depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(), year, limitServiceType),
                employeeService);
    }

    @Override
    public GetLimitDTO getByDepartmentAndYearAndLimitServiceTypeFull(UUID departmentId, Integer year, String limitServiceType) {
        var department = departmentService.get(departmentId)
                .orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));

        DepLimit depLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(), year, limitServiceType);
        GetLimitDTO getLimitDto = transformLimitEntityToDTO(depLimit, employeeService);
        if (getLimitDto != null) {
            var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(depLimit.getLimitSharingType());
            List<LimitSharing> list = limitSharingService.getByLimit(depLimit);
            getLimitDto.setLimitSharingDTOList(convertToEnrichedLimitSharingDTOList(list, limitSharingPerPeriodService));
        }
        return getLimitDto;
    }

    @Override
    public void shareLimitPrimary(
            UUID parentLimitId, List<PrimarySharingDTO> dtoList,
            JwtAuthenticationToken authentication
    ) {
        final var author = getEmployee(authentication);

        // get parent department limit
        var parentDepLimit = depLimitService.get(parentLimitId).orElseThrow(() -> new EntityNotFoundException(DepLimit.class, parentLimitId));
        // if parent department is main throw exception
        if (parentDepLimit.getDepartment().getParentId() != null) {
            throw new LimitLogicException("Этот метод только для распределения лимита головного подразделения");
        }

        depLimitService.shareDepLimitPrimary(parentDepLimit, dtoList, author);
    }

    @Override
    public List<UUID> shareLimitSecondary(
            UUID parentLimitId, List<DepLimitSharingDTO> dtoList,
            JwtAuthenticationToken authentication, String source
    ) {
        final var author = getEmployee(authentication);
        // get parent department limit
        var parentDepLimit = depLimitService.get(parentLimitId).orElseThrow(() -> new EntityNotFoundException(DepLimit.class, parentLimitId));
        var secondarySharingDataList = depLimitService.shareDepLimitSecondary(parentDepLimit, dtoList, author);
        return secondarySharingDataList.stream().map(SecondarySharingData::getLimitId).toList();
    }

    @Override
    public void shareLimitEconomy(
            UUID parentLimitId, List<DepLimitEconomyDTO> dtoList,
            JwtAuthenticationToken authentication
    ) {
        final var author = getEmployee(authentication);
        // get parent department limit
        var parentDepLimit = depLimitService.get(parentLimitId).orElseThrow(() -> new EntityNotFoundException(DepLimit.class, parentLimitId));
        for (DepLimitEconomyDTO depLimitEconomyDTO : dtoList) {
            if (depLimitEconomyDTO.getSum().compareTo(BigDecimal.ZERO) == 0) {
                throw new LimitLogicException("Сумма распределения экономии не может быть равной нулю!");
            }
        }
        depLimitService.shareDepLimitEconomy(parentDepLimit, dtoList, author);
    }

    @Override
    public void reShareLimitBetweenTransportTypes(
            LimitReSharingByTransportTypeDTO dto,
            JwtAuthenticationToken authentication
    ) {
        var author = getEmployee(authentication);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        var depLimit = depLimitService.get(dto.getLimitId()).orElseThrow(
                () -> new EntityNotFoundException(DepLimit.class, dto.getLimitId()));
        if (depLimit.getYear() < currentYear) {
            throw new LimitLogicException(CANNOT_SHARE_PREV_YEAR_ERROR);
        }
        // make transfer
        final var sum = dto.getSum();
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new LimitLogicException("Сумма не может быть меньше нуля!");
        }
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        Period fromPeriod;
        Period toPeriod;
        if (LimitSharingType.QUARTER.equals(depLimit.getLimitSharingType())) {
            fromPeriod = Optional.ofNullable(dto.getFromPeriod()).map(idx -> Quarter.values()[idx]).orElse(null);
            toPeriod = Optional.ofNullable(dto.getToPeriod()).map(idx -> Quarter.values()[idx]).orElse(null);
        } else {
            fromPeriod = Optional.ofNullable(dto.getFromPeriod()).map(idx -> Month.values()[idx]).orElse(null);
            toPeriod = Optional.ofNullable(dto.getToPeriod()).map(idx -> Month.values()[idx]).orElse(null);
        }
        var source = new LimitData(depLimit, dto.getSourceTransportType(), fromPeriod);
        var target = new LimitData(depLimit, dto.getTargetTransportType(), toPeriod);
        limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL,
                false);
    }

    @Override
    public void reShareLimitDepartments(
            LimitReSharingByDepartmentDTO dto,
            JwtAuthenticationToken authentication,
            String source
    ) {
        controller.reShareLimitDepartments(versionConverter.toV2(dto), authentication, source);
    }

    @Override
    public void reShareLimit(LimitResharingDTO dto, JwtAuthenticationToken authentication) {
        var author = getEmployee(authentication);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        var sourceDepLimit =
                depLimitService.get(dto.getSourceLimitId()).orElseThrow(() -> new EntityNotFoundException(Limit.class, dto.getSourceLimitId()));
        // get source limit by department
        var targetDepLimit =
                depLimitService.get(dto.getTargetLimitId()).orElseThrow(() -> new EntityNotFoundException(Limit.class, dto.getTargetLimitId()));

        if (sourceDepLimit.getYear() < currentYear) {
            throw new LimitLogicException(CANNOT_SHARE_PREV_YEAR_ERROR);
        }
        if (sourceDepLimit.getYear() != targetDepLimit.getYear()) {
            throw new LimitLogicException("Год лимитов должен совпадать");
        }

        // make transfer
        final var sum = dto.getSum();
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new LimitLogicException("Сумма не может быть меньше нуля!");
        }
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        Period fromPeriod;
        Period toPeriod;
        if (LimitSharingType.QUARTER.equals(sourceDepLimit.getLimitSharingType())) {
            fromPeriod = Optional.ofNullable(dto.getFromPeriod()).map(idx -> Quarter.values()[idx]).orElse(null);
            toPeriod = Optional.ofNullable(dto.getToPeriod()).map(idx -> Quarter.values()[idx]).orElse(null);
        } else {
            fromPeriod = Optional.ofNullable(dto.getFromPeriod()).map(idx -> Month.values()[idx]).orElse(null);
            toPeriod = Optional.ofNullable(dto.getToPeriod()).map(idx -> Month.values()[idx]).orElse(null);
        }
        var source = new LimitData(sourceDepLimit, dto.getSourceTransportType(), fromPeriod);
        var target = new LimitData(targetDepLimit, dto.getTargetTransportType(), toPeriod);
        limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL,
                false);
    }

    @Override
    public List<Department> getSiblings(
            UUID departmentId,
            Integer percent,
            TransportTypeEnum transportType,
            Integer year,
            Long sum
    ) {
        var department = departmentService.get(departmentId)
                .orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));
        List<Department> siblingFullList = departmentService.getByParent(department.getParentId());
        List<Department> siblingList = new ArrayList<>();
        for (Department sibDep : siblingFullList) {
            if (sibDep.getId().equals(departmentId)) {
                continue;
            }
            DepLimit sibDepLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(sibDep.getId(),
                    year,
                    getLimitServiceTypeByTransportType(transportType).name());
            if (sibDepLimit != null) {
                var sibLimitSharing = limitSharingService.getByLimitAndTransportType(sibDepLimit, transportType);
                var requestedSum = BigDecimal.valueOf(sum);
                if (sibLimitSharing.getBalance().compareTo(requestedSum) > 0) {
                    var remainder = sibLimitSharing.getBalance().subtract(requestedSum);
                    var sibPercent = remainder.multiply(BigDecimal.valueOf(100)).divide(sibLimitSharing.getBalance(), 2, RoundingMode.HALF_EVEN);
                    if (sibPercent.compareTo(BigDecimal.valueOf(percent)) >= 0) {
                        siblingList.add(sibDep);
                    }
                }

            }
        }
        return siblingList;
    }

    private Employee getEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeService.getByUserId(userId).orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));
    }

    private ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}