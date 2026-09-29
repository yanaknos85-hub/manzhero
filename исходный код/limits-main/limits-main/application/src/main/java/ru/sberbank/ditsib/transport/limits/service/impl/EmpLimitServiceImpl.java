package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.EmpLimitRepository;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class EmpLimitServiceImpl implements EmpLimitService {
    
    private final LimitService limitService;
    
    private final EmpLimitRepository empLimitRepository;
    
    private final LimitSharingService limitSharingService;
    
    private final DepartmentService departmentService;
    
    private final OrganizationService organizationService;

    private final LimitHistoryService limitHistoryService;
    
    @Qualifier("sQGeneratorLimits")
    private final SQGenerator sqGenerator;
    
    @Override
    public EmpLimit add(EmpLimit empLimit) {
        empLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        Long organizationDigitId = departmentService.get(empLimit.getEmployee().getDepartmentId())
                                                    .map(Department::getOrganizationId)
                                                    .flatMap(organizationService::get)
                                                    .map(Organization::getDigitId).orElse(null);
        EmpLimit result;
        try {
            String humanReadableId = sqGenerator.getNextId(Prefix.LU, organizationDigitId);
            empLimit.setHumanReadableId(humanReadableId);
            result = empLimitRepository.save(empLimit);
            log.info("LIMITS: EmpLimitService: add: emp limit was added with"
                     + " limit " + result.getId()
                     + " humanReadableId " + humanReadableId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public void save(EmpLimit empLimit) {
        empLimitRepository.save(empLimit);
    }
    
    @Override
    @Transactional
    public void delete(EmpLimit empLimit) {
        limitHistoryService.add(empLimit.getAuthor().getId(),
                new LimitData(empLimit, null, null),
                null,
                empLimit.getSum(), empLimit.getYear(),
                LimitHistoryType.DELETE,
                null,
                null);
        empLimitRepository.delete(empLimit);
    }
    
    @Override
    public Optional<EmpLimit> get(UUID id) {
        return empLimitRepository.findById(id);
    }
    
    @Override
    public List<EmpLimit> getAll() {
        return empLimitRepository.findAll();
    }
    
    @Override
    public List<EmpLimit> getByEmployee(Employee employee) {
        return empLimitRepository.findByEmployee(employee);
    }
    
    @Override
    public List<EmpLimit> getByEmployee(UUID employeeId) {
        return empLimitRepository.findByEmployeeId(employeeId);
    }
    
    @Override  // should not be used without serviceType!
    public EmpLimit getByEmployeeAndYear(Employee employee, Integer year) {
        List<EmpLimit> empLimitList = empLimitRepository.findByEmployeeIdAndYearAndLimitStatusNot(employee.getId(), year,
                                                                                                  LimitStatus.CLOSED);
        if (!empLimitList.isEmpty()) {
            return empLimitList.getFirst();
        }
        return null;
    }
    
    @Override
    @Transactional
    public EmpLimit getByEmployeeAndYearAndLimitServiceType(Employee employee, Integer year, String limitServiceType) {
        List<EmpLimit> empLimitList = empLimitRepository.findByEmployeeIdAndYearAndLimitServiceTypeAndLimitStatusNot(employee.getId(), year,
                                                                                                                     limitServiceType, LimitStatus.CLOSED);
        if (!empLimitList.isEmpty()) {
            return empLimitList.getFirst();
        }
        return null;
    }
    
    @Override
    public EmpLimit getEmpLimit(Employee employee, Employee author, DepLimit parentDepLimit) {
        // get limit by department
        EmpLimit empLimit = getByEmployeeAndYearAndLimitServiceType(employee, parentDepLimit.getYear(),
                                                                    parentDepLimit.getLimitServiceType());
        // if not found - create one
        if (empLimit == null) {
            empLimit = new EmpLimit();
            empLimit.setLimitType(LimitType.EMPLOYEE);
            empLimit.setAuthor(author);
            empLimit.setOrganization(parentDepLimit.getOrganization());
            empLimit.setParentDepartment(parentDepLimit.getDepartment());
            empLimit.setYear(parentDepLimit.getYear());
            empLimit.setSum(BigDecimal.ZERO);
            empLimit.setLimitSharingType(parentDepLimit.getLimitSharingType());
            empLimit.setLimitServiceType(parentDepLimit.getLimitServiceType());
            empLimit.setLimitStatus(LimitStatus.SHARED);
            empLimit.setFinalSharing(true);
            empLimit.setUseThisLimit(true);
            empLimit.setParent(parentDepLimit);
            empLimit.setEmployee(employee);
            empLimit.setLimitOwner(employee);
            empLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            empLimit = add(empLimit);
            parentDepLimit.getChildren().add(empLimit);
        }
        return empLimit;
    }
    
    @Override
    @Transactional
    public EmpLimit createEmpLimitAndTransfer(Employee employee, Employee author,
                                              DepLimit parentDepLimit, BigDecimal sum,
                                              TransportTypeEnum transportType) {
        log.info("DEBUG: createEmpLimitAndTransfer start");
        EmpLimit empLimit = getEmpLimit(employee, author, parentDepLimit);
        LimitSharing limitSharing = limitSharingService.getLimitSharing(empLimit, transportType, author);
        log.info("DEBUG: createEmpLimitAndTransfer: limit sharing found for " + transportType.getName() +
                 " with id " + limitSharing.getId());
        var source = new LimitData(parentDepLimit, transportType);
        var target = new LimitData(empLimit, transportType);
        limitService.transferSum(source, target, sum, author.getId(), false);
        return empLimit;
    }
    
    @Override
    @Transactional
    public void closeEmpLimit(EmpLimit empLimit, UUID authorId) {
        // get emp limit sharings
        var limitSharingList = limitSharingService.getByLimit(empLimit);
        for (var limitSharing : limitSharingList) {
            var source = new LimitData(empLimit, limitSharing.getTransportType());
            var target = new LimitData(empLimit.getParent(), limitSharing.getTransportType());
            limitService.transferSum(source, target, limitSharing.getBalance(), authorId, false);
        }
        empLimit.setLimitStatus(LimitStatus.CLOSED);
        save(empLimit);
    }
}
