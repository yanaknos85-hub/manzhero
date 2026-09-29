package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.limits.providers.Services;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.dto.v3.SecondarySharingData;
import ru.sberbank.ditsib.transport.limits.exceptions.ExportException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("java:S6300")
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepLimitServiceImpl implements DepLimitService {

    private static AtomicBoolean closeLimitsInProcess = new AtomicBoolean();

    private static final String DONE_FLAG = ".done";

    private static final String FAIL_FLAG = ".fail";

    private final DepLimitRepository depLimitRepository;

    private final LimitService limitService;

    private final OrganizationService organizationService;

    private final EmployeeService employeeService;

    private final EmpLimitService empLimitService;

    private final DepartmentService departmentService;

    private final LimitSharingService limitSharingService;

    private final LimitHistoryService limitHistoryService;

    @Qualifier("sQGeneratorLimits")
    private final SQGenerator sqGenerator;

    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    private final LimitSharingPercentService limitSharingPercentService;

    private final LimitSpendingService limitSpendingService;

    private final ObjectProvider<DepLimitServiceImpl> depLimitServicesProvider;

    private final Services services;

    @Override
    @Transactional
    public DepLimit add(DepLimit depLimit) {
        depLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        Long organizationDigitId =
                organizationService.get(depLimit.getDepartment().getOrganizationId()).map(Organization::getDigitId)
                        .orElse(null);
        String humanReadableId = sqGenerator.getNextId(Prefix.LD, organizationDigitId);
        depLimit.setHumanReadableId(humanReadableId);
        var result = depLimitRepository.save(depLimit);
        log.info("LIMITS: DepLimitService: add: dep limit was added with limit {} humanReadableId {}", result.getId(), humanReadableId);

        limitHistoryService.add(depLimit.getAuthor().getId(),
                new LimitData(result, null, null), null,
                depLimit.getSum(), depLimit.getYear(),
                LimitHistoryType.OPEN,
                null,
                null);
        return result;
    }

    @Override
    @Transactional
    public void save(DepLimit depLimit) {
        depLimitRepository.save(depLimit);
    }

    @Override
    @Transactional
    public void delete(DepLimit depLimit) {
        limitHistoryService.add(depLimit.getAuthor().getId(),
                new LimitData(depLimit, null, null),
                null,
                depLimit.getSum(), depLimit.getYear(),
                LimitHistoryType.DELETE,
                null,
                null);
        depLimitRepository.delete(depLimit);
    }

    @Override
    public Optional<DepLimit> get(UUID id) {
        return depLimitRepository.findById(id);
    }

    @Override
    public List<DepLimit> getAll() {
        return depLimitRepository.findAll();
    }

    @Override
    public List<DepLimit> getByDepartment(UUID departmentId) {
        return depLimitRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<DepLimit> findAccessible(UUID departmentId) {
        List<DepLimit> limits;
        do {
            var finalDepartment = departmentId;
            var department = departmentService.get(finalDepartment).orElseThrow(() -> new EntityNotFoundException(Department.class, finalDepartment));
            limits = depLimitRepository.findByDepartmentIdAndYearAndLimitStatus(department.getId(), OffsetDateTime.now().getYear(), LimitStatus.SHARED);
            var parentId = department.getParentId();
            if (parentId != null) {
                departmentId = parentId;
            } else {
                break;
            }
        } while (limits.isEmpty());
        return limits;
    }

    @Override
    public DepLimit getByDepartmentAndYear(UUID departmentId, Integer year) {
        List<DepLimit> depLimitList = depLimitRepository.findByDepartmentIdAndYear(departmentId, year);
        if (!depLimitList.isEmpty()) {
            return depLimitList.getFirst();
        }
        return null;
    }

    @Override
    public List<DepLimit> getByDepartmentAndYearAll(UUID departmentId, Integer year) {
        return depLimitRepository.findByDepartmentIdAndYear(departmentId, year);
    }

    @Override
    public DepLimit getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType) {
        return getByDepartmentAndYearAndLimitServiceType(departmentId, year, limitServiceType, true);
    }

    @Override
    public DepLimit getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType, boolean withChildren) {
        if (withChildren) {
            return depLimitRepository.findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(departmentId, year, limitServiceType, LimitStatus.CLOSED)
                    .orElse(null);
        } else {
            return depLimitRepository.findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNotWithNoChildren(departmentId, year, limitServiceType, LimitStatus.CLOSED)
                    .orElse(null);
        }
    }

    @Override
    @Transactional
    public void shareDepLimitPrimary(
            DepLimit depLimit, List<PrimarySharingDTO> dtoList,
            Employee author
    ) {
        // action
        for (var dto : dtoList) {
            final var sum = dto.getSum();
            if (sum.compareTo(BigDecimal.ZERO) <= 0) {
                throw new LimitLogicException("shareDepLimitPrimary: сумма должна быть больше нуля");
            }

            if (depLimit.getReserve().subtract(sum).compareTo(BigDecimal.ZERO) < 0) {
                throw new LimitNotSufficientException(depLimit.getHumanReadableId());
            }
            var limitSharing = limitSharingService.getLimitSharing(depLimit, dto.getTransportType(), author);
            depLimit.setReserve(depLimit.getReserve().subtract(sum));
            save(depLimit);
            limitSharingService.changeLimitSharingSumAndBalance(limitSharing, sum);
            // change limitsharing per period
            if (limitSharing.isDistributed()) {
                limitSharingService.distributeSharingPerPeriod(limitSharing);
            }
        }
    }

    @Override
    @Transactional
    public List<SecondarySharingData> shareDepLimitSecondary(DepLimit parentDepLimit,
                                                             List<DepLimitSharingDTO> dtoList,
                                                             Employee author) {
        log.info("DEBUG: share secondary start");
        // сначала перевести статус и распределить на периоды родительский лимит
        if (LimitStatus.PLANNING.equals(parentDepLimit.getLimitStatus())) {
            parentDepLimit.setLimitStatus(LimitStatus.SHARED);
            save(parentDepLimit);
        }
        for (var limitSharing : limitSharingService.getByLimit(parentDepLimit)) {
            limitSharingService.getLimitSharing(parentDepLimit, limitSharing.getTransportType(), author);
            log.info("DEBUG: limit sharing was shared and distributed for: {}", limitSharing.getTransportType().name());
        }
        if (!dtoList.isEmpty()) {
            if (parentDepLimit.isFinalSharing()) {
                throw new LimitLogicException("Распределение невозможно: У родительского лимита стоит галочка 'Конечное распределение'");
            }
            if (parentDepLimit.isUseThisLimit()) {
                throw new LimitLogicException("Распределение невозможно: У родительского лимита стоит галочка 'Использовать лимит моего подразделения'");
            }
        }
        // теперь выделить дочерние лимиты на подразделения
        var list = new ArrayList<SecondarySharingData>();
        // handle limits own sharings (create and share per period)
        for (var depLimitSharingDTO : dtoList) {
            // get target department
            var targetDepartment =
                    departmentService.get(depLimitSharingDTO.getTargetDepartmentId()).orElseThrow(
                            () -> new EntityNotFoundException(Department.class, depLimitSharingDTO.getTargetDepartmentId()));
            var targetDepLimit = getDepLimit(parentDepLimit, targetDepartment, author);
            list.add(new SecondarySharingData(targetDepLimit.getId(), targetDepartment.getId()));
            for (DepLimitSharingPerTransportDTO sptDTO : depLimitSharingDTO.getSharingPerTransportList()) {
                final var sum = sptDTO.getSum();
                if (sum.compareTo(BigDecimal.ZERO) == 0) {
                    throw new LimitLogicException("Сумма распределения не может быть равной нулю!");
                }
                limitSharingService.getLimitSharing(targetDepLimit, sptDTO.getTransportType(), author);
                var source = new LimitData(parentDepLimit, sptDTO.getTransportType(), null);
                var target = new LimitData(targetDepLimit, sptDTO.getTransportType(), null);
                limitService.transferSum(source, target, sum, author.getId(), false);
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void shareDepLimitEconomy(DepLimit mainDepLimit, List<DepLimitEconomyDTO> dtoList, Employee author) {
        if (mainDepLimit.getParent() != null) {
            throw new LimitLogicException("Только головной лимит может быть распределене по экономии");
        }
        if (mainDepLimit.getLimitStatus() != LimitStatus.SHARED) {
            throw new LimitLogicException("Только головной лимит в статусе SHARED может быть распределене по экономии");
        }
        // handle limits own sharings (create and share per period)
        for (DepLimitEconomyDTO dto : dtoList) {
            // get target department
            Department targetDepartment =
                    departmentService.get(dto.getTargetDepartmentId()).orElseThrow(
                            () -> new EntityNotFoundException(Department.class, dto.getTargetDepartmentId()));
            DepLimit targetDepLimit =
                    getByDepartmentAndYearAndLimitServiceType(targetDepartment.getId(), mainDepLimit.getYear(), mainDepLimit.getLimitServiceType());
            if (targetDepLimit == null) {
                throw new LimitLogicException("Экономия: целевое подразделение должно обладать выделенным лимитом!");
            }
            if (targetDepLimit.getParent() == null) {
                throw new LimitLogicException("Экономия: распределение в головной лимит не разрешено");
            }
            final var sum = dto.getSum();
            if (sum.compareTo(BigDecimal.ZERO) <= 0) {
                throw new LimitLogicException("Экономия: сумма должна быть больше нуля");
            }
            if (mainDepLimit.getEconomy().subtract(sum).compareTo(BigDecimal.ZERO) < 0) {
                throw new LimitNotSufficientException(mainDepLimit.getHumanReadableId());
            }

            DepLimit currentDepLimit = targetDepLimit;
            while (currentDepLimit.getParent() != null) {
                limitSharingService.getLimitSharing(currentDepLimit, dto.getTransportType(), author);
                currentDepLimit = (DepLimit) currentDepLimit.getParent();
            }

            // transfer from main reserve to shared reserve
            mainDepLimit.setEconomy(mainDepLimit.getEconomy().subtract(sum));
            save(mainDepLimit);
            LimitSharing mainDepLimitSharing = limitSharingService.getLimitSharing(mainDepLimit,
                    dto.getTransportType(), author);
            limitSharingService.changeLimitSharingSumAndBalance(mainDepLimitSharing, sum);
            limitSharingService.distributeSharingPerPeriod(mainDepLimitSharing);

            // transfer from shared reserve to target department limit
            var source = new LimitData(mainDepLimit, dto.getTransportType(), null);
            var target = new LimitData(targetDepLimit, dto.getTransportType(), null);
            limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.FROM_ECONOMY, false);
            limitHistoryService.add(author.getId(),
                    new LimitData(mainDepLimit, dto.getTransportType(), null),
                    new LimitData(targetDepLimit, dto.getTransportType(), null),
                    sum, mainDepLimit.getYear(),
                    LimitHistoryType.TRANSFER_FROM_ECONOMY,
                    mainDepLimitSharing,
                    null);
        }
    }

    /**
     * Find or create limit.
     *
     * @param department     target department.
     * @param author         author
     * @param parentDepLimit parent limit
     * @return found or created limit
     */
    private DepLimit getDepLimit(DepLimit parentDepLimit, Department department,
                                 Employee author) {
        // get limit by department
        DepLimit depLimit =
                getByDepartmentAndYearAndLimitServiceType(department.getId(), parentDepLimit.getYear(), parentDepLimit.getLimitServiceType());
        // if not found - create one
        if (depLimit == null) {
            Employee limitOwner = null;
            if (department.getDepartmentHead() != null) {
                limitOwner = employeeService.get(department.getDepartmentHead().getId()).orElse(null);
            }
            depLimit = new DepLimit();
            depLimit.setLimitType(LimitType.DEPARTMENT);
            depLimit.setAuthor(author);
            depLimit.setOrganization(parentDepLimit.getOrganization());
            depLimit.setParentDepartment(parentDepLimit.getDepartment());
            depLimit.setYear(parentDepLimit.getYear());
            depLimit.setSum(BigDecimal.ZERO);
            depLimit.setReserve(BigDecimal.ZERO);
            depLimit.setEconomy(BigDecimal.ZERO);
            depLimit.setLimitSharingType(parentDepLimit.getLimitSharingType());
            depLimit.setLimitServiceType(parentDepLimit.getLimitServiceType());
            depLimit.setUseThisLimit(false);
            depLimit.setLimitStatus(LimitStatus.PLANNING);
            depLimit.setFinalSharing(false);
            depLimit.setParent(parentDepLimit);
            depLimit.setDepartment(department);
            depLimit.setLimitOwner(limitOwner);
            depLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            depLimit = add(depLimit);
            parentDepLimit.getChildren().add(depLimit);
        }
        return depLimit;
    }

    private List<String> closeChildrenLimits(Limit limit, UUID authorId, JwtAuthenticationToken authentication, String appName, String source) {
        int count = 0;
        List<Limit> childrenList = new ArrayList<>(limitService.getLimitChildren(limit, null));
        log.debug("closeChildrenLimits: for limitId " + limit.getId() + " found children limits " + childrenList.size());
        try {
            for (var child : childrenList) {
                count += closeLimit(child, authorId);
            }
        } catch (Exception e) {
            log.error("Error while closing limits!", e);
            String reason = e.getMessage() == null ? "reason unknown" : e.getMessage();
            return List.of(reason);
        }
        log.debug("closeChildrenLimits: closed limits {}", count);
        return List.of("Закрыто лимитов " + count);
    }

    private int closeLimit(Limit limit, UUID authorId) {
        log.debug("closeLimit: limitId {}", limit.getId());
        int count = 0;
        if (!limit.getChildren().isEmpty()) {
            var children = limit.getChildren();
            for (var child : children) {
                count += closeLimit(child, authorId);
            }
        }
        boolean closedSuccess = limitService.closeLimit(limit, authorId);
        count += (closedSuccess ? 1 : 0);
        return count;
    }

    @Override
    @Transactional
    public Integer deleteLimits(UUID organizationId, String serviceType, Integer year, Employee author) {
        int count = 0;
        List<Limit> depLimitList = limitService.getUpperLevelLimitList(organizationId, serviceType, year);
        log.debug("deleteLimits: found upper level limits " + depLimitList.size());
        for (Limit depLimit : depLimitList) {
            count += deleteLimit(depLimit);
        }
        log.debug("deleteLimits: deleted limits " + count);
        return count;
    }

    @Override
    @Transactional
    public Integer deleteLimits(UUID organizationId, Employee author) {
        int count = 0;
        List<Limit> depLimitList = limitService.getUpperLevelActiveLimit(organizationId);
        log.debug("deleteLimits: found upper level limits " + depLimitList.size());
        for (Limit depLimit : depLimitList) {
            count += deleteLimit(depLimit);
        }
        log.debug("deleteLimits: deleted limits " + count);
        return count;
    }

    private int deleteLimit(Limit limit) {
        int count = 0;
        if (!limit.getChildren().isEmpty()) {
            var children = limit.getChildren();
            for (var child : children) {
                int deleted = deleteLimit(child);
                count += deleted;
            }
        }
        deleteLimitActual(limit);
        count += 1;
        return count;
    }

    private void deleteLimitActual(Limit limit) {
        Optional<LimitSharingPercents> limitSharingPercent = limitSharingPercentService.getByLimit(limit);
        limitSharingPercent.ifPresent(limitSharingPercentService::delete);

        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limit);
        var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(limit.getLimitSharingType());
        for (LimitSharing limitSharing : limitSharingList) {
            var limitSharingPerPeriodList = limitSharingPerPeriodService.getByLimitSharing(limitSharing);
            for (var limitSharingPerPeriod : limitSharingPerPeriodList.values()) {
                List<LimitSpending> limitSpendingList = limitSpendingService.getByLimitSharingPerPeriod(limitSharingPerPeriod);
                for (LimitSpending limitSpending : limitSpendingList) {
                    limitSpendingService.delete(limitSpending);
                }
                limitSharingPerPeriodService.delete(limitSharingPerPeriod);
            }
            limitSharingService.delete(limitSharing);
        }
        if (limit.getLimitType() == LimitType.DEPARTMENT) {
            delete((DepLimit) limit);
        }
        if (limit.getLimitType() == LimitType.EMPLOYEE) {
            empLimitService.delete((EmpLimit) limit);
        }
    }

    @Override
    public void printLimits(UUID organizationId, Integer year, String str) {
        log.info(str);
        List<Department> departmentList = departmentService.getUpperLevelDepartment(organizationId);
        if (departmentList.size() != 1) {
            log.info("printLimit: ERROR: не найден уникальный верхнеуровневый департамент. количество: {}", departmentList.size());
        }
        for (final var serviceType : services.get()) {
            List<Limit> upperLimitList = limitService.getUpperLevelLimitList(organizationId, serviceType.name(), year);
            if (upperLimitList.size() > 1) {
                log.info("printLimit: ERROR: найдено более одного верхнеуровневого лимита для услуги {}", serviceType);
            }
            if (upperLimitList.size() == 1) {
                log.info("printLimit: найден один верхнеуровневый лимит для услуги {} id {}", serviceType, upperLimitList.getFirst().getId());
                DepLimit upperLimit = (DepLimit) upperLimitList.getFirst();
                printLimit(upperLimit);
            }
        }
    }

    @Override
    public boolean checkHaveNotClosedChildLimits(List<UUID> ids) {
        return depLimitRepository.checkHaveNotClosedChildLimits(ids);
    }

    private void printLimit(Limit limit) {
        List<LimitSharing> limitSharings = limitSharingService.getByLimit(limit);
        final var sumLimitSharings = limitSharings.stream().map(LimitSharing::getSum).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        log.info("printLimit:  limit {} limit_parent {} type = {} sum = {} num of children = {} num of sharings = {} sum of sharings = {}", limit.getId(), limit.getParent() == null ? "null" : limit.getParent().getId(), limit.getLimitType(), limit.getSum(), limit.getChildren().size(), limitSharings.size(), sumLimitSharings);
        for (var limitSharing : limitSharings) {
            log.info("printLimit:  for limit {} limitSharing = {} with transport type = {} sum = {} balance = {}", limit.getId(), limitSharing.getId(), limitSharing.getTransportType(), limitSharing.getSum(), limitSharing.getBalance());
        }
        if (!limit.getChildren().isEmpty()) {
            final var children = limit.getChildren();
            for (var child : children) {
                printLimit(child);
            }
        }
    }

    @Override
    public Optional<File> getAsyncFile(String fileName) {
        return getFile(getFileRelativePath(fileName));
    }

    /**
     * Получение файла.
     *
     * @param fileName имя файла.
     * @return файл.
     */
    @SneakyThrows(IOException.class)
    public Optional<File> getFile(String fileName) {
        fileName = getTempDirectory(fileName);
        var path = Path.of(fileName);
        var file = path.toFile();
        if (file.isFile() && Files.exists(Path.of(file.getAbsolutePath() + DONE_FLAG))) {
            return Optional.of(file);
        }
        if (Files.exists(Path.of(file.getAbsolutePath() + FAIL_FLAG))) {
            throw new ExportException(Files.readAllLines(Path.of(file.getAbsolutePath() + FAIL_FLAG)));
        }
        return Optional.empty();
    }

    @SneakyThrows(IOException.class)
    private String getTempDirectory(String fileName) {
        var filePath = Path.of(System.getProperty("java.io.tmpdir", "."), fileName);
        var directory = filePath.getParent();
        if (!directory.toFile().isDirectory()) {
            Files.createDirectories(directory);
        }
        return filePath.toAbsolutePath().toString();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TaskStartedDto auditLimitsAsync(UUID organizationId, Integer year, boolean logOkRecords) {
        String prefix = "audit";
        var fileName = createFileName(prefix);
        depLimitServicesProvider.getObject().auditLimitsRunAsync(getFileRelativePath(fileName), organizationId, year, logOkRecords);
        return TaskStartedDto.builder().started(true).url(fileName).build();
    }

    @Override
    public TaskStartedDto closeLimitsAsync(UUID authorId, UUID limitId, JwtAuthenticationToken authentication, String appName, String source) {
        if (closeLimitsInProcess.get()) {
            return TaskStartedDto.builder().url("").started(false).build();
        }
        var depLimit = get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        var prefix = "closeLimits";
        var fileName = createFileName(prefix);
        depLimitServicesProvider.getObject().closeLimitsRunAsync(getFileRelativePath(fileName), depLimit, authorId, authentication, appName, source);
        return TaskStartedDto.builder().started(true).url(fileName).build();
    }

    private String createFileName(String url) {
        var now = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "%1$s-%2$s.%3$s".formatted(url, now, "XLS");
    }

    private String getFileRelativePath(String fileName) {
        return "/files/limits/result/%s".formatted(fileName);
    }

    @Async
    @SneakyThrows(IOException.class)
    @Transactional
    public void auditLimitsRunAsync(String tempFile, UUID organizationId, Integer year, boolean logOkRecords) {
        tempFile = getTempDirectory(tempFile);
        List<String> auditList = auditLimits(organizationId, year, logOkRecords);
        String auditStr = StringUtils.join(auditList, "|");

        var filePath = Path.of(tempFile).toAbsolutePath().toString();
        try (PrintWriter p = new PrintWriter(new FileOutputStream(filePath))) {
            p.print(auditStr);
        } catch (Exception e) {
            log.error("Exporting limits failed", e);
            try (var fileOutputStream = new FileOutputStream(filePath + FAIL_FLAG)) {
                fileOutputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        log.info("File exported to %s".formatted(tempFile));
        try (var fileOutputStream = new FileOutputStream(filePath + DONE_FLAG)) {
            fileOutputStream.write(new byte[0]);
        }
    }

    @Async
    @SneakyThrows(IOException.class)
    @Transactional
    public void closeLimitsRunAsync(String tempFile, Limit limit, UUID authorId, JwtAuthenticationToken authentication, String appName, String source) {
        closeLimitsInProcess.set(true);
        log.debug("closeLimitsRunAsync: limitId " + limit.getId());
        tempFile = getTempDirectory(tempFile);
        List<String> list = closeChildrenLimits(limit, authorId, authentication, appName, source);
        String auditStr = StringUtils.join(list, "|");

        var filePath = Path.of(tempFile).toAbsolutePath().toString();
        try (PrintWriter p = new PrintWriter(new FileOutputStream(filePath))) {
            p.print(auditStr);
        } catch (Exception e) {
            log.error("Exporting limits failed", e);
            try (var fileOutputStream = new FileOutputStream(filePath + FAIL_FLAG)) {
                fileOutputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        log.info("File exported to %s".formatted(tempFile));
        try (var fileOutputStream = new FileOutputStream(filePath + DONE_FLAG)) {
            fileOutputStream.write(new byte[0]);
        }
        closeLimitsInProcess.set(false);
    }

    @Override
    @Transactional
    public List<String> auditLimits(UUID organizationId, Integer year, boolean logOkRecords) {
        List<String> results = new ArrayList<>();
        checkDepartments(organizationId, logOkRecords, results);

        for (var serviceType : services.get()) {
            checkLimits(organizationId, year, logOkRecords, results, serviceType.name());
        }
        return results;
    }

    private void checkLimits(UUID organizationId, Integer year, boolean logOkRecords, List<String> results, String serviceType) {
        var upperLimitList = limitService.getUpperLevelActiveLimitList(organizationId, serviceType, year);
        if (upperLimitList.size() > 1) {
            results.add("auditLimits: ERROR: найдено более одного верхнеуровневого лимита для услуги " + serviceType);
        }
        if (upperLimitList.isEmpty() && logOkRecords) {
            results.add("auditLimits: ОК: не найден верхнеуровневый лимит для услуги " + serviceType);
        }
        if (upperLimitList.size() == 1) {
            DepLimit upperLimit = (DepLimit) upperLimitList.getFirst();
            // алгоритм
            // аккумулированные по дереву лимитов limit.sum
            // + нераспределенные остатки по видам транспорта из головного
            // + резерв (нераспределенная общая куча) = общая сумма
            // для каждого limit сумма его limitsharing.sum = limit.sum
            if (logOkRecords) {
                results.add("auditLimits: ОК: найден верхнеуровневый лимит для услуги " + serviceType);
            }
            var sumOfSublimits = BigDecimal.ZERO;
            for (Limit limit : upperLimit.getChildren()) {
                sumOfSublimits = sumOfSublimits.add(sumUpLimit(limit, results, serviceType));
            }
            var unsharedLimitSharings = limitSharingService.getByLimit(upperLimit)
                    .stream()
                    .map(LimitSharing::getSum)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            if (sumOfSublimits.add(unsharedLimitSharings).add(upperLimit.getReserve()).compareTo(upperLimit.getSum()) != 0) {
                results.add("auditLimits: ERROR: аудит не сходится для услуги " + serviceType
                        + " всего выделено " + upperLimit.getSum()
                        + " из них в общем резерве " + upperLimit.getReserve()
                        + ", распределено на ВТ, но не распределено на другие лимиты " + unsharedLimitSharings
                        + ", распределено на другие лимиты " + sumOfSublimits);
            } else {
                if (logOkRecords) {
                    results.add("auditLimits: ОК: аудит сходится для услуги " + serviceType);
                }
            }
        }
    }

    private void checkDepartments(UUID organizationId, boolean logOkRecords, List<String> results) {
        var departmentList = departmentService.getUpperLevelDepartment(organizationId);
        if (departmentList.size() != 1) {
            results.add("auditLimits: ERROR: не найден уникальный верхнеуровневый департамент. количество: " + departmentList.size());
        } else {
            if (logOkRecords) {
                results.add("auditLimits: ОК: найден верхнеуровневый департамент с id " + departmentList.getFirst().getId());
            }
        }
    }

    private BigDecimal sumUpLimit(Limit limit, List<String> results, String serviceType) {
        if (limit.getSum().compareTo(BigDecimal.ZERO) < 0) {
            results.add("auditLimits: ERROR: ошибка в аудите  для услуги %s для лимита %s сумма лимита отрицательна %s"
                    .formatted(serviceType, limit.getId(), limit.getSum()));
        }
        var count = BigDecimal.ZERO;
        if (!limit.getChildren().isEmpty()) {
            var children = limit.getChildren();
            for (var child : children) {
                var sum = sumUpLimit(child, results, serviceType);
                count = count.add(sum);
            }
        }
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limit);
        var sumLimitSharings = BigDecimal.ZERO;
        for (LimitSharing limitSharing : limitSharingList) {
            if (limitSharing.getSum().compareTo(BigDecimal.ZERO) < 0) {
                results.add(
                        "auditLimits: ERROR: ошибка в аудите  для услуги %s для лимита %s сумма limitSharing отрицательна %s".formatted(
                                serviceType, limit.getId(), limitSharing.getSum()));
            }
            if (limitSharing.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                results.add(
                        "auditLimits: ERROR: ошибка в аудите  для услуги %s для лимита %s баланс limitSharing отрицателен %s".formatted(
                                serviceType, limit.getId(), limitSharing.getBalance()));
            }
            sumLimitSharings = sumLimitSharings.add(limitSharing.getSum());
        }
        if (sumLimitSharings.compareTo(limit.getSum()) != 0) {
            results.add("auditLimits: ERROR: ошибка в аудите "
                    + " для услуги " + serviceType
                    + " для лимита " + limit.getId()
                    + " сумма распределений на виды транспорта " + sumLimitSharings
                    + " не равна сумме лимита " + limit.getSum());
        }
        count = count.add(limit.getSum());
        return count;
    }
}
