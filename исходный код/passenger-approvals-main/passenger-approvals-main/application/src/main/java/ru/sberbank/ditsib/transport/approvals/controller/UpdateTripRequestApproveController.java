package ru.sberbank.ditsib.transport.approvals.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with approvals controller.
 **/
@RequestMapping("/update/trip")
@Tag(name = "Согласование маршрутов поездки",
     description = "Набор операций для работы с согласованиями изменений маршрутов поездки")
public interface UpdateTripRequestApproveController {
    /**
     * Approve final trip.
     */
    @PutMapping("/approve/{approveId}")
    @Operation(summary = "Согласование", description = "Подтверждение согласования изменения маршрута поездки")
    void approve(
            @NotNull @PathVariable("approveId") UUID approveId);
    
    /**
     * Decline approval.
     *
     * @param approveId id of approve
     * @param cancelDTO object containing decline reasons
     */
    @PutMapping("/decline/{approveId}")
    @Operation(summary = "Отклонение", description = "Отклонение согласования изменениея маршрута поездки")
    void decline(@NotNull @PathVariable("approveId") UUID approveId,
                 @RequestBody @Valid CancelDTO cancelDTO);
    
    /**
     * Get active approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/active", "/list/active/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка активных согласований изменения маршрута поездок",
               description = "Получение списка активных согласований изменения маршрута поездок")
    Collection<TripApproveDTO> getActiveApprovals(
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * Get closed approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/closed",  "/list/closed/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка завершенных согласований изменения маршрутов поездок",
               description = "Получение списка завершенных согласований изменения маршрутов поездок")
    Collection<TripApproveDTO> getClosedApprovals();
    
    @GetMapping(value = "{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение активного согласования изменения маршрутов поездок по ID заявки",
               description = "Используется при изменении маршута согласующим")
    TripApproveDTO getApprovalByRequestId(@NotNull @PathVariable("requestId") UUID requestId);
}
