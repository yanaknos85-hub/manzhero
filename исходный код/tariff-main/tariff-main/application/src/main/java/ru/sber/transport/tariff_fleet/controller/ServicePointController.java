package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;

/**
 * Контроллер по точкам обслуживания
 */
@RequestMapping("service-points")
@Tag(name = "Точки обслуживания", description = "Точки обслуживания ТС")
public interface ServicePointController {
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Валидация точек обслуживания", description = "Валидация файла с данными точек обслуживания")
    UploadServicePointsDto validateUploadServicePoints(
            @RequestPart("documentType") String documentType,
            @RequestPart("file") MultipartFile file
                                                      );
    
    @GetMapping(value = "file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Получение шаблона", description = "Скачать шаблон для загрузки точек обслуживания")
    byte[] downloadServicePointsTemplate();
}
