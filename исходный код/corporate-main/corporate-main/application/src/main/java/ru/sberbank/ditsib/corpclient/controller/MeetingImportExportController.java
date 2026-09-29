package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Deprecated
@RequestMapping("/files/meetingAddress/")
public interface MeetingImportExportController {

    /**
     * Эскпорт данных в файл.
     *
     * @param request входящий запрос.
     * @return файл.
     */
    @Operation(summary = "Экспорт", description = "Экспорт данных в файл")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<byte[]> exportFile(HttpServletRequest request);

    /**
     * Эскпорт данных в файл.
     *
     * @param request  входящий запрос.
     * @return файл.
     */
    @Operation(summary = "Экспорт", description = "Экспорт данных в файл")
    @GetMapping(value = "/result/{fileName}/", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> exportFileWithName(HttpServletRequest request);

    /**
     * Эскпорт данных в файл.
     *
     * @param request входящий запрос.
     * @return файл.
     */
    @Operation(summary = "Экспорт", description = "Получение шаблона")
    @GetMapping(value = "/empty/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<byte[]> exportEmptyBook(HttpServletRequest request);

    /**
     * Эскпорт данных в файл.
     *
     * @param request входящий запрос.
     * @return файл.
     */
    @Operation(summary = "Экспорт", description = "Экспорт данных в файл для сложных тарифов")
    @GetMapping(value = "/{id}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<byte[]> exportBook(HttpServletRequest request);

    /**
     * Импорт данных из файла.
     *
     * @param request входящий запрос.
     * @return данные о выгрузке.
     */
    @Operation(summary = "Импорт", description = "Импорт данных из файла")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<byte[]> importFile(HttpServletRequest request, @RequestParam("file") MultipartFile file);

    /**
     * Получение результата импорта
     *
     * @param request входящий запрос.
     * @return результаты импорта.
     */
    @Operation(summary = "Получить результат выгрузки",
            description = "Получить результат запущенный выгрузок текущего пользователя")
    @GetMapping(value = "/result/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<byte[]> getResult(HttpServletRequest request);
}
