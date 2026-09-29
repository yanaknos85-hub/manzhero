package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.file_works.controller.ExportController;
import ru.sber.transport.file_works.controller.ImportController;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.dto.ResultDto;
import ru.sber.transport.file_works.dto.TaskStartedDto;
import ru.sber.transport.file_works.exceptions.ResultNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.ImportStatus;
import ru.sberbank.ditsib.transport.limits.controller.ImportExportController;
import ru.sberbank.ditsib.transport.limits.dto.ImportResultDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsService;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@Scope("request")
class ImportExportControllerImpl implements ImportExportController {

    private static final String LIMITS_IMPORT_URL = "limits";

    private static final String ORGANIZATION_ID_PARAMETER = "organizationId";

    private final LimitStatsService limitStatsService;

    private final ExportController exportController;

    private final ImportController importController;

    @CheckOrganizationAccess
    @Override
    public ResponseEntity<TaskStartedDto> exportLimitsAsync(@Organization UUID organizationId, Integer year, JwtAuthenticationToken authentication) {
        var response = exportController.exportFile(LIMITS_IMPORT_URL, ru.sber.transport.file_works.properties.FileType.XLSX,
            MultiValueMap.fromSingleValue(Map.of(ORGANIZATION_ID_PARAMETER, organizationId, "year", year)));
        var body = response.getBody();
        assert body != null;
        return ResponseEntity.ok().headers(response.getHeaders())
            .body(new TaskStartedDto(Paths.get(body.getUrl()).getFileName().toString(), body.getStarted()));
    }

    @Override
    public ResponseEntity<byte[]> getAsyncFile(String fileName) {
        return exportController.exportFile(LIMITS_IMPORT_URL, fileName);
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public ImportResultDTO importLimits(UUID organizationId, MultipartFile file, JwtAuthenticationToken authentication) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var started = executor.submit(() -> {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                var result = importController.importFile(LIMITS_IMPORT_URL, MultiValueMap.fromSingleValue(Map.of(ORGANIZATION_ID_PARAMETER, organizationId, "dryRun", false)), file);
                ResultDto data;
                do {
                    data = getData(result);
                } while (data == null || !data.getFinished());
                return data;
            }).get();
            return convertResult(started);
        }
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @CheckOrganizationAccess
    @Override
    public ImportResultDTO importLimitsSimulation(
        @Organization UUID organizationId,
        MultipartFile file, JwtAuthenticationToken authentication) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var started = executor.submit(() -> {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                var result = importController.importFile(LIMITS_IMPORT_URL, MultiValueMap.fromSingleValue(Map.of(ORGANIZATION_ID_PARAMETER, organizationId, "dryRun", true)), file);
                ResultDto data;
                do {
                    data = getData(result);
                } while (data == null || !data.getFinished());
                return data;
            }).get();
            return convertResult(started);
        }
    }

    @SneakyThrows(IOException.class)
    @CheckOrganizationAccess
    @Override
    public ResponseEntity<InputStreamResource> exportLimitStats(@Organization UUID organizationId,
                                                Integer year,
                                                Integer month,
                                                Integer day,
                                                UUID departmentId,
                                                JwtAuthenticationToken authentication) {
        var untilDate = LocalDate.of(year, month, day);
        var limitList = limitStatsService.getLimitsList(organizationId, year, departmentId);
        var limitStatsDTOs = limitStatsService.getLimitStats(
            organizationId,
            limitList.stream().filter(it -> it.getLimit() instanceof DepLimit)
            .toList(),
            untilDate);
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("Report_%s.xlsx".formatted(untilDate.format(DateTimeFormatter.ISO_LOCAL_DATE))).build().toString())
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(new FileInputStream(limitStatsService.exportToXlsx(limitStatsDTOs).toFile())));
    }

    @NotNull
    private ImportResultDTO convertResult(ResultDto data) {
        log.info("Import started");
        var importResult = new ImportResultDTO();
        var totalErrors = new ArrayList<String>();
        totalErrors.addAll(data.getPages().parallelStream().map(PageInfo::getExceptionStrings).flatMap(Collection::parallelStream).toList());
        totalErrors.addAll(data.getPages().parallelStream().map(PageInfo::getRow).flatMap(Collection::parallelStream).map(r -> "%s) %s".formatted(r.getRowNumber(), getData(r.getExceptionStrings(), r.getViolations()))).toList());
        importResult.setResultStatus(totalErrors.isEmpty() ? ImportStatus.SUCCESS : ImportStatus.FAIL);
        importResult.setErrorDescrs(totalErrors);
        importResult.setInserted(data.getPages().parallelStream().mapToLong(PageInfo::getRowProcessed).sum());
        importResult.setTotal(data.getPages().parallelStream().mapToLong(PageInfo::getRowCount).sum());
        importResult.setErrors(data.getPages().parallelStream().map(PageInfo::getRow).flatMap(Collection::parallelStream).filter(p -> !p.getViolations().isEmpty() && !p.getExceptionStrings().isEmpty()).count());
        log.info("Import finished");
        return importResult;
    }

    private String getData(List<String> exceptions, List<Map<String, Object>> violations) {
        var viols = Optional.ofNullable(violations).orElseGet(List::of).parallelStream()
            .flatMap(map -> map.entrySet().parallelStream())
            .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(System.lineSeparator()));
        var exes = exceptions.parallelStream().collect(Collectors.joining(System.lineSeparator()));
        return "%s%s%s".formatted(viols, viols.isBlank() ? "" : System.lineSeparator(), exes);
    }

    private ResultDto getData(TaskStartedDto started) {
        try {
            return importController.getResult(started.getUrl().replace("files/", "").replace("/result", "")).getLast();
        } catch (ResultNotFoundException ignore) {
            return null;
        }
    }

}
