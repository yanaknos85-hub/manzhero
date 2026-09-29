package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.controller.ServicePointController;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.service.ServicePointService;

@RestController
@RequiredArgsConstructor
public class ServicePointControllerImpl implements ServicePointController {
    
    private final ServicePointService servicePointService;
    
    
    @Override
    public UploadServicePointsDto validateUploadServicePoints(String documentType, MultipartFile file) {
        return servicePointService.validateUploadServicePoints(DocumentType.resolveDocumentType(documentType), file);
    }
    
    @Override
    public byte[] downloadServicePointsTemplate() {
        return servicePointService.downloadServicePointsTemplate();
    }
}
