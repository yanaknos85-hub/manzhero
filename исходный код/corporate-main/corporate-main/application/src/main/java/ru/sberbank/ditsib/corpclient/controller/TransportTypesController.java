package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.corpclient.dto.constant.TransportTypeEnumDTO;

import java.util.List;

/**
 * Контроллер типов транпорта
 */
@RequestMapping({"/transport-types", "/transport-types/"})
@Tag(name = "типы транспорта", description = "Набор операций для работы с типами транспорта")
public interface TransportTypesController {
    
    /**
     * Запрос списка допустимых типов транспорта
     *
     * @return list of transport types.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех типов транспорта")
    List<TransportTypeEnumDTO> getAll();
}
