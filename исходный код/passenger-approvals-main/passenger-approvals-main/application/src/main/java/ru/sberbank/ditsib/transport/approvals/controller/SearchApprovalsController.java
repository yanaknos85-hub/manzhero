package ru.sberbank.ditsib.transport.approvals.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.CountResponseDTO;
import ru.sberbank.ditsib.transport.approvals.dto.Type;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;

import java.util.List;

/**
 * Контроллер для поиска согласований.
 **/
@RequestMapping("/")
@Tag(name = "Поиск согласований", description = "Набор операций для поиска согласований")
public interface SearchApprovalsController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка согласований по поиску",
            description = "Получение списка согласований поездок по поиску")
    Page<ApprovalJournalDto> getApprovals(
            @Parameter(description = "Список статусов для фильтрации")
            @RequestParam(required = false) List<Status> status,
            @Parameter(description = "Список типов согласования для фильтрации")
            @RequestParam(required = false) List<Type> type,
            @Parameter(description = "Тип ТС для получения")
            @RequestParam(required = false) String transportType,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault Pageable pageable,
            @Parameter(description = "Параметры поиска по пассажиру") EmployeeSearchParams employeeSearchParams
    );

    /**
     * Get count active approvals
     *
     * @return count of approvals.
     */
    @GetMapping(value = "/active/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение количества активных согласований",
            description = "Получение количества активных согласований")
    CountResponseDTO getCountActiveApprovals();
}
