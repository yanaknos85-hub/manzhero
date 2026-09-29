package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.file_works.dto.TaskStartedDto;
import ru.sberbank.ditsib.transport.limits.dto.ImportResultDTO;

import java.util.UUID;

@Deprecated(since = "2023-11-29")
@RequestMapping(value = {"/limits", "/limits/"})
public interface ImportExportController {

    /**
     * Limits loading
     *
     * @param organizationId .
     * @param file .
     *
     * @return result of loading
     */
    @PostMapping(value = {"/import/{organizationId}", "/import/{organizationId}/"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ImportResultDTO importLimits(
            @PathVariable("organizationId") UUID organizationId,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Limits loading simulation
     *
     * @param organizationId .
     * @param file .
     *
     * @return result of loading
     */
    @PostMapping(value = {"/import/simulation/{organizationId}", "/import/simulation/{organizationId}/"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Загрузка лимитов эмуляция", description = "Загрузка лимитов эмуляция")
    ImportResultDTO importLimitsSimulation(
            @PathVariable("organizationId") UUID organizationId,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Limits export
     *
     * @param year year
     * @return result of export
     * @deprecated use `GET /statistic?organizationId={organizationId}&amp;year={year}&amp;month={month}&amp;day={day}` with accept=application/octet-stream instead
     */
    @GetMapping(value = {"/exportstats/{organizationId}/{year}/{month}/{day}", "/exportstats/{organizationId}/{year}/{month}/{day}/"},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Экспорт статистики по лимитам в XLS",
            description = "Экспорт статистики по лимитам в XLS")
    ResponseEntity<InputStreamResource> exportLimitStats(@PathVariable("organizationId") UUID organizationId,
                            @PathVariable("year") Integer year,
                            @PathVariable("month") Integer month,
                            @PathVariable("day") Integer day,
                            @RequestParam(required = false) UUID departmentId,
                            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Limits export
     *
     * @param year year
     *
     * @return result of export
     */
    @GetMapping(value = "/exportAsync/org/{organizationId}/year/{year}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Экспорт лимитов асинхронный", description = "Экспорт лимитов")
    ResponseEntity<TaskStartedDto> exportLimitsAsync(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("year") Integer year,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Эскпорт данных в файл.
     *
     * @param fileName название файла.
     *
     * @return файл.
     */
    @Operation(summary = "Получение файла", description = "Получене файла созданном в асинхронном режиме")
    @GetMapping(value = "/getAsync/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> getAsyncFile(
            @Parameter(description = "Путь к экспортируемым данным", required = true) @PathVariable("fileName") String fileName
    );
}
