package ru.sberbank.ditsib.transport.approvals.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.approvals.controller.SearchApprovalsController;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.CountResponseDTO;
import ru.sberbank.ditsib.transport.approvals.dto.Type;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.services.SearchService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.LinkedList;
import java.util.List;

/**
 * Имплементация контроллера поиска согласований.
 */
@RequiredArgsConstructor
@E2EController
@RestController
@Slf4j
class SearchApprovalControllerImpl implements SearchApprovalsController {
    
    private final SearchService searchService;
    
    @Override
    public Page<ApprovalJournalDto> getApprovals(List<Status> status,
                                                 List<Type> types,
                                                 String transportType,
                                                 Pageable pageable,
                                                 EmployeeSearchParams employeeSearchParams) {
        var userId = ControllerUtils.currentUser();
        return searchService.searchForJournal(userId, status, types, transportType, pageable, employeeSearchParams);
    }
    
    @Override
    public CountResponseDTO getCountActiveApprovals() {
        var status = List.of(Status.NEW, Status.EDITED);
        var userId = ControllerUtils.currentUser();
        var types = new LinkedList<Type>();
        var count = searchService.searchCount(status, userId, types);
        return CountResponseDTO.builder().count(count).build();
    }
}
