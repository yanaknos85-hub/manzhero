package ru.sberbank.ditsib.corpclient.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for delegate assigning, editing etc.
 */
public interface DelegateService {
    
    /**
     * Add new delegate
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param newDelegateRecordDTO new delegate dto
     *
     * @return created Delegate record
     */
    GetDelegateRecordDTO add(
            UUID organizationId,
            UUID departmentId,
            @Valid DelegateRecordDTO newDelegateRecordDTO
                            );
    
    
    /**
     * Edit specified delegate.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param recordId ID of delegate record
     * @param updatedDelegateRecord new data of delegate
     *
     * @return modified Delegate record
     */
    GetDelegateRecordDTO edit(
            UUID organizationId,
            UUID departmentId,
            UUID recordId,
            DelegateRecordDTO updatedDelegateRecord
                             );
    
    /**
     * Delete delegate record by id
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param recordId ID of delegate record
     */
    void delete(
            UUID organizationId,
            UUID departmentId,
            UUID recordId
               );

    /**
     * Delete status update.
     */
    void updateDelegateStatusByDeadline();

    /**
     * Get list of all delegates of specified supervisor
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param supervisorId supervisor id
     * @param pageable size
     *
     * @return list of delegate records
     */
    Page<GetDelegateRecordDTO> getAllBySupervisor(
            UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            Pageable pageable
    );

    /**
     * Get list of all delegates of specified supervisor
     *
     * @return delegate record
     */
    GetDelegateRecordDTO getById(UUID id);

    /**
     * Get list of all delegates of specified supervisor
     *
     * @param userId supervisor id
     *
     * @return list of delegate records
     */
    List<GetDelegateRecordDTO> getAllBySupervisor(
            UUID userId
                                                 );

    /**
     * Get list of all delegates of specified supervisor
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param supervisorId supervisor id
     * @param pageable size
     *
     * @return list of delegate records
     */
    Page<GetDelegateRecordDTO> getAllBySupervisorByDate(
            UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            LocalDate localDate,
            Pageable pageable
    );

    /**
     * Get list of all delegates of specified supervisor
     *
     * @param userId ID of user.
     * @param localDate date to get delegation data.
     *
     * @return list of delegate records
     */
    List<GetDelegateRecordDTO> getAllBySupervisorByDate(
            UUID userId,
            LocalDate localDate
                                                       );
    
    /**
     * Get list of all delegates records that specified employee participates in
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param delegateId delegate id
     *
     * @return list of delegate records
     */
    List<GetDelegateRecordDTO> getAllByDelegate(
            UUID organizationId,
            UUID departmentId,
            UUID delegateId
                                               );
    
    /**
     * Получить список делегированных полномочий указанному делегату
     *
     * @param delegateId Id делегата
     * @param localDate дата на которую осуществляется выборка
     *
     * @return список делегированных полномочий
     */
    List<GetDelegateRecordDTO> getAllByDelegate(
            UUID delegateId,
            LocalDate localDate
                                               );
    
    /**
     * Get delegate record by id
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param delegateRecordId delegateRecordId
     *
     * @return delegate record
     */
    GetDelegateRecordDTO get(
            UUID organizationId,
            UUID departmentId,
            UUID delegateRecordId
                            );
    
    /**
     * Get all delegate candidates for specified parameters
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param supervisorId supervisor id
     * @param transportType transport type
     * @param startDate date to search for valid candidates
     *
     * @return get collection of delegate candidates.
     */
    Iterable<EmployeeDTO> getAllCandidates(
            UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            TransportTypeEnum transportType,
            @FutureOrPresent LocalDate startDate, EmployeeParameters parameters
                                          );
    
    /**
     * Получить список кандидатов для пользователя
     *
     * @param supervisorId id пользователя
     * @param transportType transport type
     * @param startDate date to search for valid candidates
     *
     * @return get collection of delegate candidates.
     */
    Iterable<EmployeeDTO> getAllCandidatesForUserAndTransportType(
            UUID supervisorId,
            TransportTypeEnum transportType,
            @FutureOrPresent LocalDate startDate, EmployeeParameters parameters
                                                                 );
}
