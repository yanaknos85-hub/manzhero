package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.approvals.constant.JournalSortProperty;
import ru.sberbank.ditsib.transport.approvals.database.dao.ApprovalJournalRepository;
import ru.sberbank.ditsib.transport.approvals.database.dao.FraudDataRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.Type;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.exception.InvalidSortPropertyException;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalJournalMapper;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeRightService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.approvals.services.SearchService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
class SearchServiceImpl implements SearchService {

    private final EmployeeRightService employeeRightService;
    private final ApprovalJournalRepository approvalJournalRepository;
    private final FraudDataRepository fraudDataRepository;
    private final ApprovalJournalMapper approvalJournalMapper;
    private final EmployeeService employeeService;

    @Override
    public Page<ApprovalJournalDto> searchForJournal(UUID userId,
                                                     List<Status> statuses,
                                                     List<Type> types,
                                                     String transportType,
                                                     Pageable pageable,
                                                     EmployeeSearchParams employeeSearchParams) {
        final var allowedDepartments = employeeRightService.getAllowedDepartments(userId);
        final var departmentIds = allowedDepartments.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final var employeeIds = employeeService.getAllByDepartmentIds(departmentIds);
        final var changedStatuses = Optional.ofNullable(statuses).orElse(Arrays.asList(Status.values())).stream()
                .map(Status::name)
                .toList();
        final var transportTypesToSearchIn = getAllowedTransportTypes(allowedDepartments.keySet(), transportType);
        validateSortProperties(pageable.getSort());
        final var checkedPageable = addDefaultSortIfUnsorted(pageable);
        final var allTransportTypes = transportTypesToSearchIn == null || transportTypesToSearchIn.isEmpty();
        var result = approvalJournalRepository.findAllForJournal(
                changedStatuses,
                employeeIds,
                allTransportTypes,
                transportTypesToSearchIn,
                userId,
                checkedPageable);
        var ids = result.getContent().stream()
                .map(ApprovalJournalProjection::getId)
                .collect(Collectors.toSet());
        var frauds = fraudDataRepository.findAllByApprovalIdIn(ids).stream()
                .collect(Collectors.groupingBy(fraudData -> fraudData.getApproval().getId()));
        return new PageImpl<>(
                result.getContent().stream()
                        .map(approvalJournalProjection ->
                                approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                                        frauds.get(approvalJournalProjection.getId())))
                        .toList(),
                result.getPageable(),
                result.getTotalElements());
    }

    @Override
    public void saveApprovalJournalForSharedRideJoinApproval(SharedRideJoinApproval savedApproval) {
        var optionalApprovalJournal = approvalJournalRepository.findApprovalJournalByApprovalId(savedApproval.getId());
        if (optionalApprovalJournal.isPresent()) {
            approvalJournalRepository.save(approvalJournalMapper.sharedRideJoinApprovalToApprovalJournal(savedApproval, optionalApprovalJournal.get().getId()));
        } else {
            approvalJournalRepository.save(approvalJournalMapper.sharedRideJoinApprovalToApprovalJournal(savedApproval, null));
        }
    }

    @Override
    public void saveApprovalJournalForTripRequestApproval(TripRequestApproval savedApproval) {
        var optionalApprovalJournal = approvalJournalRepository.findApprovalJournalByApprovalId(savedApproval.getId());
        if (optionalApprovalJournal.isPresent()) {
            approvalJournalRepository.save(approvalJournalMapper.tripRequestApprovalToApprovalJournal(savedApproval, optionalApprovalJournal.get().getId()));
        } else {
            approvalJournalRepository.save(approvalJournalMapper.tripRequestApprovalToApprovalJournal(savedApproval, null));
        }
    }

    @Override
    public void saveApprovalJournalForFinalTripApproval(FinalTripApproval savedApproval) {
        var optionalApprovalJournal = approvalJournalRepository.findApprovalJournalByApprovalId(savedApproval.getId());
        if (optionalApprovalJournal.isPresent()) {
            approvalJournalRepository.save(approvalJournalMapper.finalTripApprovalToApprovalJournal(savedApproval, optionalApprovalJournal.get().getId()));
        } else {
            approvalJournalRepository.save(approvalJournalMapper.finalTripApprovalToApprovalJournal(savedApproval, null));
        }
    }

    @Override
    public void saveApprovalJournalForUpdateTripRequestApproval(UpdateTripRequestApproval savedApproval) {
        var optionalApprovalJournal = approvalJournalRepository.findApprovalJournalByApprovalId(savedApproval.getId());
        if (optionalApprovalJournal.isPresent()) {
            approvalJournalRepository.save(approvalJournalMapper.updateTripRequestApprovalToApprovalJournal(savedApproval, optionalApprovalJournal.get().getId()));
        } else {
            approvalJournalRepository.save(approvalJournalMapper.updateTripRequestApprovalToApprovalJournal(savedApproval, null));
        }
    }

    @Override
    public long searchCount(@NonNull List<Status> statuses,
                            UUID userId,
                            List<Type> types
    ) {
        var allowedDepartments = employeeRightService.getAllowedDepartments(userId);
        var departmentIds = allowedDepartments.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final var employeeIds = employeeService.getAllByDepartmentIds(departmentIds);
        var changedStatuses = statuses.stream()
                .map(Status::name)
                .toList();
        var transportTypesToSearchIn = getAllowedTransportTypes(allowedDepartments.keySet(), null);
        return approvalJournalRepository.countForJournal(
                changedStatuses,
                employeeIds,
                transportTypesToSearchIn == null || transportTypesToSearchIn.isEmpty(),
                transportTypesToSearchIn,
                userId
        );
    }

    private Pageable addDefaultSortIfUnsorted(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(JournalSortProperty.CREATION_TIME.getSortName()).descending());
        }
        return pageable;
    }

    private static void validateSortProperties(Sort sort) {
        if (sort.isSorted()) {
            var hasInvalidSortProperties = sort.stream()
                    .anyMatch(order -> Arrays.stream(JournalSortProperty.values())
                            .noneMatch(journalSort -> journalSort.getSortName().equals(order.getProperty())));
            if (hasInvalidSortProperties) {
                throw new InvalidSortPropertyException();
            }
        }
    }

    private static Set<String> getAllowedTransportTypes(Set<String> allowedTypes, String typeToSearch) {
        if (typeToSearch == null) {
            var checkedTypes = allowedTypes.stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            return checkedTypes.isEmpty() ? null : checkedTypes;
        }
        return Set.of(typeToSearch);
    }
}