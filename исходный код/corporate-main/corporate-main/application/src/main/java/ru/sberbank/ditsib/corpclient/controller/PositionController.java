package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.NewPositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller for working with positions.
 */
@RequestMapping({"/{organizationId}/positions", "/{organizationId}/positions/"})
@Tag(name = "Должности", description = "Набор операций для работы с должностями")
public interface PositionController {
    
    /**
     * @param orgId organization Id
     *
     * @return request mapping string for specified id
     */
    static String getApiMappingByOrgId(@NotNull UUID orgId) {
        return "/{organizationId}/positions/".replace("{organizationId}", orgId.toString()) + "/";
    }
    
    /**
     * Add a new position to existing organization
     *
     * @param organizationId organization Id
     * @param newPosition new position data.
     *
     * @return added position.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление новой должности")
    PositionDTO savePosition(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody NewPositionDTO newPosition
                            );
    
    /**
     * Edit position.
     *
     * @param organizationId organization Id
     * @param newData new data of position.
     */
    @PutMapping(value = {"{positionId}", "{positionId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных должности")
    void editPosition(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("positionId") UUID positionId,
            @Valid @RequestBody PositionDTO newData
                     );
    
    /**
     * Delete position.
     *
     * @param organizationId organization Id
     * @param positionId ID of position to delete.
     */
    @DeleteMapping(value = {"{positionId}", "{positionId}/"})
    @Operation(summary = "Удаление", description = "Удаление(дезактивация) данных должности")
    void deletePosition(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("positionId") UUID positionId
                       );
    
    /**
     * Восстановление удаленной должности
     *
     * @param organizationId organization Id
     * @param positionId id должности для восстановления.
     */
    @PutMapping(value = {"{positionId}", "{positionId}/"})
    @Operation(summary = "Удаление", description = "Восстановление удаленной должности")
    void restorePosition(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("positionId") UUID positionId
                       );
    
    /**
     * Get position with ID.
     *
     * @param organizationId organization Id
     * @param positionId ID of position to get.
     *
     * @return position.
     */
    @GetMapping(value = {"{positionId}", "{positionId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных должности")
    PositionDTO getPosition(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("positionId") UUID positionId
                           );
    
    /**
     * Get all positions.
     *
     * @param organizationId organization Id
     *
     * @return list of positions.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех должностей")
    Collection<PositionDTO> getPositions(@PathVariable("organizationId") UUID organizationId);
    
    /**
     * Search positions.
     *
     * @param positionSearchDTO positionSearchDTO
     *
     * @return list of positions.
     */
    @PostMapping(value = {"/searchPositions", "/searchPositions/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск должностей", description = "Поиск должностей")
    Collection<PositionDTO> searchPositions(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody PositionSearchDTO positionSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
