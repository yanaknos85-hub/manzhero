package ru.sberbank.ditsib.transport.approvals.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with approvals controller.
 **/
@RequestMapping("/request/trip")
@Tag(name = "Согласование заявок", description = "Набор операций для работы с согласованиями заявок")
public interface TripRequestApproveController {
    
    /**
     * Approve editable request.
     */
    @PutMapping("/approve/{approveId}")
    @Operation(summary = "Согласование", description = "Подтверждение согласования заявки на поездку")
    void approve(
            @NotNull @PathVariable("approveId") UUID approveId
    );
    
    /**
     * Decline approval.
     *
     * @param approveId id of approve
     * @param cancelDTO object containing decline reasons
     */
    @PutMapping("/decline/{approveId}")
    @Operation(summary = "Отклонение", description = "Отклонение согласования заявки на поездку")
    void decline(
            @NotNull @PathVariable("approveId") UUID approveId,
            @RequestBody @Valid CancelDTO cancelDTO
    );
    
    /**
     * Get active approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = "/page/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка активных согласований поездок",
               description = "Получение списка активных согласований поездок. Сортировка выполняется сначала по полю, " +
                             "указанному в page, потом (при наличии) по параметру пассажира (EmployeeSearchParams.field).")
    Page<TripApproveDTO> getPageActiveApprovals(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Parameter EmployeeSearchParams approveParams,
            @PageableDefault(size = 20) Pageable page);
    
    /**
     * Get closed approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = "/page/closed", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение постраничного списка завершенных согласований поездок",
               description = "Получение постраничного списка завершенных согласований поездок")
    Page<TripApproveDTO> getPageClosedApprovals(
            @PageableDefault(size = 20) Pageable page);

    /**
     * Get active approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = "/list/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка активных согласований поездок",
            description = "Получение списка активных согласований поездок")
    Collection<TripApproveDTO> getActiveApprovals(JwtAuthenticationToken authentication);

    /**
     * Get closed approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/closed", "/list/closed/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка завершенных согласований поездок",
            description = "Получение списка завершенных согласований поездок")
    Collection<TripApproveDTO> getClosedApprovals();
    
}
