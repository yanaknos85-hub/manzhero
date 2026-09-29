package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.EconomyV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;

import java.util.List;
import java.util.UUID;

@RequestMapping("/economy/{limitId}")
public interface EconomyController {



    /**
     * Get economy stats.
     *
     * @return list of economy stats.
     */
    @GetMapping(value = "/future", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение рассчетной экономии за год", description = "Получение экономии за год")
    List<EconomyV2DTO> getTransfersToEconomy(
            @RequestParam(value = "year", required = false) Integer year,
            @PathVariable("limitId") @NotNull UUID limitId
    );

    /**
     * Get economy stats.
     *
     * @return list of economy stats.
     */
    @GetMapping(value = "/past", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение фактической экономии за год", description = "Получение экономии за год")
    List<GetLimitTransferHistoryV2DTO> getTransfersFromEconomy(
            @RequestParam(value = "year", required = false) Integer year,
            @PathVariable("limitId") @NotNull UUID limitId
    );

}
