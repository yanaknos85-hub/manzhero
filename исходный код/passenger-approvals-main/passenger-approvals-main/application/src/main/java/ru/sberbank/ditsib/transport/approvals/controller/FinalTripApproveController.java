package ru.sberbank.ditsib.transport.approvals.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;

import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with approvals controller.
 **/
@RequestMapping("/final/trip")
@Tag(name = "Согласование поездок", description = "Набор операций для работы с согласованиями поездки")
public interface FinalTripApproveController {
    /**
     * Approve final trip.
     */
    @PutMapping("/approve/{approveId}")
    @Operation(summary = "Согласование", description = "Подтверждение согласования финальной поездки")
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
    @Operation(summary = "Отклонение", description = "Отклонение согласования финальной поездки")
    void decline(
            @NotNull @PathVariable("approveId") UUID approveId,
            @RequestBody @Valid CancelDTO cancelDTO
    );
    
    /**
     * Get active approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/active", "/list/active/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка активных согласований финальных поездок",
               description = "Получение списка активных согласований финальных поездок")
    Collection<TripApproveDTO> getActiveApprovals(
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * Get closed approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/closed", "/list/closed/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка завершенных согласований финальных поездок",
               description = "Получение списка завершенных согласований финальных поездок")
    Collection<TripApproveDTO> getClosedApprovals();
    
    
}
