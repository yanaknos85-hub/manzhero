package ru.sberbank.ditsib.transport.approvals.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.SharedRideApproveDTO;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with approvals controller.
 **/
@RequestMapping("/shared/ride")
@Tag(name = "Согласование присоединений к СП", description = "Набор операций для работы с согласованиями " +
                                                             "присоединения к совместной личной поездке")
public interface SharedRideApproveController {
    /**
     * Approve final trip.
     */
    @PutMapping("/approve/{approveId}")
    @Operation(summary = "Согласование", description = "Подтверждение согласования присоединения к совместной " +
                                                       "личной поездке")
    void approve(
            @NotNull @PathVariable("approveId") UUID approveId
    );
    
    /**
     * Decline approval.
     *
     * @param approveId id of approve
     */
    @PutMapping("/decline/{approveId}")
    @Operation(summary = "Отклонение", description = "Отклонение согласования присоединения к совместной личной поездке")
    void decline(
            @NotNull @PathVariable("approveId") UUID approveId
    );
    
    /**
     * Get active approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/active", "/list/active/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка активных согласований присоединения к совместной личной поездке",
               description = "Получение списка активных согласований присоединения к совместной личной поездке")
    Collection<SharedRideApproveDTO> getActiveApprovals(
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * Get closed approvals
     *
     * @return list of approvals.
     */
    @GetMapping(value = {"/list/closed", "/list/closed/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка завершенных согласований присоединения к совместной личной поездке",
               description = "Получение списка завершенных согласований присоединения к совместной личной поездке")
    Collection<SharedRideApproveDTO> getClosedApprovals();
    
    
}
