package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/groups")
@Tag(name ="Группы организаций", description = "Набор операций для работы с группами организаций")
public interface OrganizationGroupController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление группы организаций")
    OrganizationGroupResponseDTO add(@RequestBody @Valid OrganizationGroupDTO organizationGroupDTO) throws JsonProcessingException;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение всех групп организаций")
    Page<OrganizationGroupResponseDTO> getAll(OrganizationGroupSearchDTO organizationGroupSearchDTO);

    @PutMapping(value = "/{organizationGroupId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование", description = "Редактирование группы организаций")
    void update(@PathVariable("organizationGroupId") UUID organizationGroupId,
               @RequestBody @Valid OrganizationGroupDTO organizationGroupDTO);

    @PatchMapping(value = "/{organizationGroupId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Частичное редактирование", description = "Частичное редактирование группы организаций")
    void patch(@PathVariable("organizationGroupId") UUID organizationGroupId,
                @RequestBody List<PatchData<OrganizationGroupPatchFields>> fields);

    @DeleteMapping(value = "/{organizationGroupId}")
    @Operation(summary = "Удаление", description = "Удаление группы организаций")
    void delete(@PathVariable("organizationGroupId") UUID organizationGroupId);

}
