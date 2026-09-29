package ru.sber.transport.tariff_fleet.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ServicePointRepository;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.service.RepairAndFuelServicePointService;
import ru.sber.transport.tariff_fleet.service.ServicePointService;
import ru.sber.transport.tariff_fleet.service.excel.ExcelExportProperties;
import ru.sber.transport.tariff_fleet.service.excel.impl.BaseExcelExporter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.sber.transport.tariff_fleet.constant.DocumentType.FUEL;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.REPAIR_AND_MAINTENANCE;


@Service
@RequiredArgsConstructor
public class ServicePointServiceImpl implements ServicePointService {

    private static final Set<DocumentType> SUPPORTED_DOCUMENT_TYPES = Set.of(REPAIR_AND_MAINTENANCE, FUEL);

    private final RepairAndFuelServicePointService repairAndFuelServicePointService;
    private final ServicePointRepository repository;

    @Override
    public void saveAll(List<ServicePoint> servicePoints) {
        repository.saveAll(servicePoints);
    }

    @Override
    public UploadServicePointsDto validateUploadServicePoints(DocumentType documentType, MultipartFile file) {
        if (SUPPORTED_DOCUMENT_TYPES.contains(documentType)) {
            return repairAndFuelServicePointService.validateUploadServicePoints(file);
        } else {
            throw new UnsupportedDocumentTypeException(documentType, List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePoint> findAllByContractId(UUID contractId) {
        return repository.findAllByContractId(contractId);
    }

    @Override
    @Transactional
    public List<ServicePoint> dropExistedPointsForContractAndCreate(UUID contractId, List<ServicePointDto> servicePoints) {
        if (servicePoints != null && !servicePoints.isEmpty()) {
            repository.deleteAllByContractId(contractId);
            return repository.saveAll(servicePoints.stream()
                    .map(it -> ServicePoint.builder()
                            .contractId(contractId)
                            .address(it.address())
                            .latitude(it.latitude())
                            .longitude(it.longitude())
                            .build())
                    .toList());
        }
        return List.of();
    }

    @Override
    public byte[] downloadServicePointsTemplate() {
        return repairAndFuelServicePointService.downloadServicePointsTemplate();
    }


    @Override
    public byte[] buildServicePointsFile(List<ServicePoint> servicePoints) {
        var index = new AtomicInteger(0);
        return BaseExcelExporter.export(servicePoints.stream()
                        .map(it -> new ServicePointExcelDto(index.incrementAndGet(), it.getAddress(), it.getLatitude(),
                                it.getLongitude())).toList(), ServicePointExcelDto.class,
                new ExcelExportProperties(WorkbookType.XLSX, ServicePointExcelDto.getExcelFieldsInfo()));
    }

    @Override
    @Transactional
    public void deactivateServicePointsForContract(UUID contractId) {
        repository.deactivateByContractId(contractId);
    }
}
