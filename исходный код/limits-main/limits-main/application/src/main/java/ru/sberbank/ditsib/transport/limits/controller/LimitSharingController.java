package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingDTO;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for limit sharings.
 */
@RequestMapping(value = "/limitsharing")
@Tag(name = "Распределение лимита", description = "Набор операций для распределения лимита")
public interface LimitSharingController {
    
    /**
     * Get limit sharing with ID.
     *
     * @param limitSharingId ID of limit sharing to get.
     *
     * @return limit sharing.
     * @deprecated use `GET /sharings/{limitSharingId}` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "{limitSharingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных распределения", description = "Получение данных распределения")
    GetLimitSharingDTO get(@PathVariable("limitSharingId") @NotNull UUID limitSharingId);
    
    /**
     * Get all limit sharings.
     *
     * @return list of limit sharings.
     * @deprecated use `GET /sharings` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех распределений", description = "Получение всех распределения")
    Collection<GetLimitSharingDTO> getAll();
    
    /**
     * Get all limit sharings full.
     *
     * @return list of limit sharings full.
     * @deprecated use `GET /sharings` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех распределений c остатками за период",
               description = "Получение всех распределения c остатками за период")
    Collection<GetLimitSharingDTO> getAllFull();
    
    /**
     * Get limit sharing by limit.
     *
     * @param limitId limit id.
     *
     * @return list of limit sharing dto.
     * @deprecated use `GET /sharings?limitId={limitId}` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByLimit/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение распределения лимита", description = "Получение распределения лимита")
    List<GetLimitSharingDTO> getByLimit(@PathVariable("limitId") @NotNull UUID limitId);
    
    
    /**
     * Get limit sharing by limit full.
     *
     * @param limitId limit id.
     *
     * @return list of limit sharing dto.
     * @deprecated use `GET /sharings?limitId={limitId}` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByLimit/full/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение распределения лимита c остатками за период",
               description = "Получение распределения лимита c остатками за период")
    List<GetLimitSharingDTO> getByLimitFull(@PathVariable("limitId") @NotNull UUID limitId);
    
    /**
     * Get limit sharing by limit full.
     *
     * @param limitId limit id.
     *
     * @return list of limit sharing dto.
     */
    @GetMapping(value = "/getSharingImitation/{limitId}/{totalSum}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Имитация распределения денежных средств",
               description = "Имитация распределения денежных средств")
    List<Long> getSharingImitation(@PathVariable("limitId") @NotNull UUID limitId,
                                          @PathVariable("totalSum") @NotNull Long totalSum);
}
