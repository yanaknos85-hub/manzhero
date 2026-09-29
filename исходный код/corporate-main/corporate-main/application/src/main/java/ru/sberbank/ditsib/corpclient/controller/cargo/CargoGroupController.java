package ru.sberbank.ditsib.corpclient.controller.cargo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.cargo.CagroGroupTypeDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoGroupDto;

import java.util.Collection;
import java.util.UUID;

/**
 * Controller for working with cargo groups.
 */
@RequestMapping("/cargo/type/group")
@Tag(name = "Грузы. Справочник групп грузов", description = "Набор операций для работы со справочниками групп грузов")
public interface CargoGroupController {

    /**
     * Get all groups.
     *
     * @return list of groups.
     */
    @GetMapping(value = "/search/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех видов грузов по названию группы", description = "Получение всех активных видов грузов по названию группы")
    Collection<CagroGroupTypeDto> getTypesByGroupName(@RequestParam("name") @NotBlank String groupName,
                                                      @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Delete group.
     *
     * @param groupId ID group to delete.
     */
    @DeleteMapping(value = "/{groupId}")
    @Operation(summary = "Удаление", description = "Удаление груза из группы")
    void deleteGroup(@PathVariable("groupId") UUID groupId,
                     @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Add a new group.
     *
     * @param newData new data of group.
     * @return group.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление вида груза в группу")
    CagroGroupTypeDto addGroup(@Valid @RequestBody CargoGroupDto newData,
                               @Parameter(hidden = true) JwtAuthenticationToken authentication);

}
