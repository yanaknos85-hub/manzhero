package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;

@RequestMapping({"/self/attributes", "/self/attributes/"})
@Tag(name = "Признаки сотрудника", description = "Набор операций для работы со справочником \"Признаки сотрудника\"")
public interface AttributeController {

    /**
     * Add a new attribute.
     *
     * @param newData new data of attribute.
     *
     * @return added attribute.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление нового признака сотрудника")
    @ResponseBody
    AttributeDto addAttribute(@RequestBody @Valid NewAttributeDto newData);

    /**
     * Edit a attribute.
     *
     * @param id ID attribute to edit.
     * @param newData new data of attribute.
     */
    @PutMapping(value = {"{id}", "{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных признака сотрудника")
    void editAttribute(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AttributeDto newData
    );

    /**
     * Delete attribute.
     *
     * @param id ID attribute to delete.
     */
    @DeleteMapping(value = {"{id}", "{id}/"})
    @Operation(summary = "Удаление", description = "Удаление данных признака сотрудника")
    void deleteAttribute(@PathVariable("id") UUID id);

    /**
     * get all attribute
     *
     * @return list of attributes
     */
    @Operation(summary = "Получение всех Признаков", description = "Получение всех Признаков сотрудника")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Collection<AttributeDto> getAll();

    /**
     * get all status attribute
     *
     * @return list of attributes
     */
    @Operation(summary = "Получение признаков сотрудника", description = "Получение всех активных признако")
    @GetMapping(value = {"/active", "/active/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Collection<AttributeDto> getActive();

}
