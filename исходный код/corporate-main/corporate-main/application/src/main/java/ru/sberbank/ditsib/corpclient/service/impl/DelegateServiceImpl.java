package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sberbank.ditsib.corpclient.database.dao.DelegateRepository;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.exceptions.DataConflictException;
import ru.sberbank.ditsib.corpclient.exceptions.DataConstrainViolationException;
import ru.sberbank.ditsib.corpclient.exceptions.DelegateAlreadyExistsException;
import ru.sberbank.ditsib.corpclient.mapper.DelegateMapper;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.DelegateSender;
import ru.sberbank.ditsib.corpclient.service.DelegateService;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.ValidationService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of delegate service
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class DelegateServiceImpl implements DelegateService {

    private static final String USER_ID_FIELD = "userId";

    private final DelegateMapper mapper;

    private final EmployeeMapper employeeMapper;

    private final DelegateRepository repository;

    private final EmployeeRepository employeeRepository;

    private final DepartmentRepository departmentRepository;

    private final ValidationService validationService;

    private final DepartmentService departmentService;

    private final DelegateSender delegateSender;

    private final EmployeeService employeeService;

    @Override
    public GetDelegateRecordDTO add(
            UUID organizationId,
            UUID departmentId,
            DelegateRecordDTO newDelegateRecordDTO
    ) {
        log.info("DelegateService: add: start");
        checkAddDate(newDelegateRecordDTO);
        checkSubordination(organizationId, newDelegateRecordDTO);
        var delegateRecord = mapper.dtoToDelegate(newDelegateRecordDTO);
        if (repository
                .existsByUniqueFields(newDelegateRecordDTO.getSupervisorId(), newDelegateRecordDTO.getDelegateId(),
                        newDelegateRecordDTO.getTransportType(), newDelegateRecordDTO.getStartDate())) {
            throw new DelegateAlreadyExistsException(newDelegateRecordDTO.getSupervisorId(), newDelegateRecordDTO
                    .getDelegateId(),
                    newDelegateRecordDTO.getStartDate());
        }
        if (repository.existsByIntervalDates(newDelegateRecordDTO.getSupervisorId(), newDelegateRecordDTO
                        .getDelegateId(), newDelegateRecordDTO.getTransportType(),
                newDelegateRecordDTO.getStartDate(), newDelegateRecordDTO.getEndDate())) {
            throw new DelegateAlreadyExistsException(newDelegateRecordDTO.getSupervisorId(), newDelegateRecordDTO
                    .getDelegateId(),
                    newDelegateRecordDTO.getStartDate(), newDelegateRecordDTO
                    .getEndDate());
        }
        var savedRecord = repository.save(delegateRecord);
        var delegateId = Objects.requireNonNull(savedRecord.getDelegate().getId());
        savedRecord.setDelegate(employeeRepository.findById(delegateId).orElse(null));
        delegateSender.send(savedRecord);
        return mapper.delegateRecordToDTO(savedRecord);
    }


    @Override
    public GetDelegateRecordDTO get(UUID organizationId, UUID departmentId, UUID delegateRecordId) {
        return mapper.delegateRecordToDTO(getDelegateRecord(organizationId, departmentId, delegateRecordId));
    }

    @Override
    public GetDelegateRecordDTO getById(UUID id) {
        var record = repository.findById(id);
        return record.map(mapper::delegateRecordToDTO).orElse(null);
    }


    @Override
    public GetDelegateRecordDTO edit(
            UUID organizationId, UUID departmentId, UUID recordId, DelegateRecordDTO updatedDelegateRecord
    ) {
        log.info("DelegateService: edit: start");
        var saved = getDelegateRecord(organizationId, departmentId, recordId);

        checkUpdateDate(saved, updatedDelegateRecord);
        checkSubordination(organizationId, updatedDelegateRecord);
        if (repository
                .existsByUniqueFieldsExceptSame(saved.getId(), updatedDelegateRecord.getSupervisorId(),
                        updatedDelegateRecord.getDelegateId(),
                        updatedDelegateRecord.getTransportType(),
                        updatedDelegateRecord.getStartDate())) {
            throw new DelegateAlreadyExistsException(updatedDelegateRecord.getSupervisorId(), updatedDelegateRecord
                    .getDelegateId(),
                    updatedDelegateRecord.getStartDate());
        }

        if (repository.existsByIntervalDatesExceptSame(saved.getId(), updatedDelegateRecord.getSupervisorId(),
                updatedDelegateRecord
                        .getDelegateId(),
                updatedDelegateRecord.getTransportType(),
                updatedDelegateRecord.getStartDate(),
                updatedDelegateRecord.getEndDate())) {
            throw new DelegateAlreadyExistsException(updatedDelegateRecord.getSupervisorId(), updatedDelegateRecord
                    .getDelegateId(),
                    updatedDelegateRecord.getStartDate(), updatedDelegateRecord
                    .getEndDate());
        }

        saved.setStartDate(updatedDelegateRecord.getStartDate());
        saved.setEndDate(updatedDelegateRecord.getEndDate());
        saved.setTransportType(updatedDelegateRecord.getTransportType());
        saved = repository.save(saved);
        delegateSender.send(saved);
        return mapper.delegateRecordToDTO(saved);
    }

    @Override
    public void delete(UUID organizationId, UUID departmentId, UUID recordId) {
        var saved = getDelegateRecord(organizationId, departmentId, recordId);
        saved.setStatus(RecordStatus.INACTIVE);
        repository.save(saved);
        delegateSender.sendDelete(saved);
    }

    @Override
    public void updateDelegateStatusByDeadline() {
        log.debug("DelegateService: updateDelegateStatusByDeadline: start");
        var now = LocalDate.now();
        var delegateRecords = repository.findByStatus(RecordStatus.ACTIVE).stream()
                .filter(Objects::nonNull)
                .filter(e -> now.isAfter(e.getEndDate()))
                .peek(e -> e.setStatus(RecordStatus.INACTIVE))
                .collect(Collectors.toList());
        repository.saveAll(delegateRecords);
        delegateRecords.forEach(delegateSender::sendDelete);
        log.debug("DelegateService: updateDelegateStatusByDeadline: end");
    }

    @Override
    public Page<GetDelegateRecordDTO> getAllBySupervisor(
            UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            Pageable pageable
    ) {
        return repository.getAllBySupervisorIdOrderByDelegateIdAscStartDateAsc(supervisorId, pageable)
                .map(mapper::delegateRecordToDTO);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<GetDelegateRecordDTO> getAllBySupervisor(
            UUID userId
    ) {
        var supervisorId = employeeRepository.findByUserId(userId).map(Employee::getId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId)));
        return repository.getAllBySupervisorIdOrderByDelegateIdAscStartDateAsc(supervisorId).stream()
                .map(mapper::delegateRecordToDTO).toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Page<GetDelegateRecordDTO> getAllBySupervisorByDate(
            UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            LocalDate localDate,
            Pageable pageable
    ) {
        return repository.getAllBySupervisorIdAndDate(supervisorId, localDate, pageable)
                .map(mapper::delegateRecordToDTO);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<GetDelegateRecordDTO> getAllBySupervisorByDate(
            UUID userId,
            LocalDate localDate
    ) {
        var supervisorId = employeeRepository.findByUserId(userId).map(Employee::getId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId)));
        return repository.getAllBySupervisorIdAndDate(supervisorId, localDate).stream().map(
                mapper::delegateRecordToDTO).toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<GetDelegateRecordDTO> getAllByDelegate(
            UUID organizationId,
            UUID departmentId,
            UUID delegateId
    ) {
        return repository.getAllByDelegateIdOrderByStartDate(delegateId).stream().map(
                mapper::delegateRecordToDTO).toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<GetDelegateRecordDTO> getAllByDelegate(
            UUID userId,
            LocalDate localDate
    ) {
        var employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId)));
        return repository.getAllByDelegateIdAndDateOrderByStartDate(employee.getId(), localDate).stream().map(
                mapper::delegateRecordToDTO).toList();
    }

    @Override
    public Iterable<EmployeeDTO> getAllCandidates(
            UUID organizationId, UUID departmentId, UUID supervisorId, TransportTypeEnum transportType,
            LocalDate startDate, EmployeeParameters parameters
    ) {
        return getAllCandidatesForUserAndTransportType(supervisorId, transportType, startDate, parameters);
    }

    @Override
    public Iterable<EmployeeDTO> getAllCandidatesForUserAndTransportType(
            UUID supervisorId, TransportTypeEnum transportType,
            @FutureOrPresent LocalDate startDate, EmployeeParameters parameters
    ) {
        var byDepartmentHeadId = departmentRepository.getActiveByDepartmentHeadId(supervisorId);
        if (byDepartmentHeadId.isEmpty()) {
            return Collections.emptyList();
        }
        var excludedCandidateIds =
                repository.getAllBySupervisorIdAndTransportIdAndDate(supervisorId, transportType, startDate).stream()
                        .map(DelegateRecord::getDelegate).map(Employee::getId);

        var candidates = employeeService.getEmployeesByDepartmentIds(
                byDepartmentHeadId,
                parameters,
                Stream.concat(excludedCandidateIds, Stream.of(supervisorId)).collect(Collectors.toSet())
        );
        return candidates.map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
    }

    /**
     * @param organizationId   organization Id
     * @param departmentId     department id
     * @param delegateRecordId ID of delegate record
     * @return delegate record or throw exception
     */
    protected DelegateRecord getDelegateRecord(UUID organizationId, UUID departmentId, UUID delegateRecordId) {
        departmentService.validateDepartmentByIdAndOrgId(departmentId, organizationId);
        return repository.findBySupervisorOrganizationIdAndId(organizationId, delegateRecordId)
                .orElseThrow(
                        () -> new EntityNotFoundException(DelegateRecord.class, delegateRecordId));
    }

    protected void checkSubordination(
            UUID organizationId, DelegateRecordDTO delegateRecordDTO
    ) {
        if (delegateRecordDTO.getSupervisorId().equals(delegateRecordDTO.getDelegateId())) {
            throw new DataConflictException("Нельзя назначить делегатом самого пользователя");
        }

        var departmentIds = departmentRepository.getActiveByDepartmentHeadId(delegateRecordDTO.getSupervisorId());
        if (departmentIds.isEmpty()) {
            throw new DataConflictException(String.format("Сотрудник с идентификатором %s не является " +
                    "руководителем подразделения", delegateRecordDTO.getSupervisorId()));
        }
        log.info("checkSubordination: найдены подразделения для руководителя{}", delegateRecordDTO.getSupervisorId());
        validationService.validateDepartmentIds(organizationId, departmentIds);
        EmployeeDTO delegate = employeeService.validateAndGetEmployeeByIdAndOrgId(delegateRecordDTO.getDelegateId(), organizationId);

        if (delegate.status() == EmployeeStatus.INACTIVE) {
            throw new DataConflictException("Нельзя назначить делегатом неактивного сотрудника");
        }
    }

    protected void checkAddDate(DelegateRecordDTO delegateRecordDTO) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        checkDate(startDate, endDate, delegateRecordDTO);
    }

    protected void checkUpdateDate(DelegateRecord saved, DelegateRecordDTO delegateRecordDTO) {
        LocalDate savedStartDate = saved.getStartDate();
        LocalDate savedEndDate = saved.getEndDate();
        checkDate(savedStartDate, savedEndDate, delegateRecordDTO);
    }

    protected void checkDate(LocalDate startDate, LocalDate endDate, DelegateRecordDTO updatedDelegateRecord) {
        Map<String, Serializable> conflicted = new LinkedHashMap<>();
        LocalDate updateStartDate = updatedDelegateRecord.getStartDate();
        if (!startDate.equals(updateStartDate)
                && updateStartDate.isBefore(LocalDate.now())) {
            conflicted.put("startDate", updateStartDate);
        }

        LocalDate updateEndDate = updatedDelegateRecord.getEndDate();
        if (!endDate.equals(updateEndDate)
                && (updateEndDate.isBefore(LocalDate.now()) || updateEndDate.isBefore(updateStartDate))) {
            conflicted.put("endDate", updateEndDate);
        }

        if (!conflicted.isEmpty()) {
            throw new DataConstrainViolationException(DelegateRecordDTO.class, DataConstrainViolationException.ConflictType.DATE_CONSTRAIN_VIOLATION,
                    updatedDelegateRecord.getDelegateId(), conflicted);
        }
    }
}
