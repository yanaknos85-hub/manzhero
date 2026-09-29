package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeDocumentRepository;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sberbank.ditsib.corpclient.service.EmployeeDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {
    private final EmployeeDocumentRepository employeeDocumentRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployeeAccessToCar(UUID employeeId, UUID carId) {
        return employeeDocumentRepository.existsByIdEmployeeIdAndCarId(employeeId, carId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDocument> findValidationDocuments(UUID employeeId, UUID carId, LocalDateTime desiredDate) {
        return employeeDocumentRepository.findValidationDocuments(employeeId, carId, desiredDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployeeActiveDriverLicense(UUID employeeId, LocalDateTime desiredDate) {
        return employeeDocumentRepository.isDriverLicenseValidForDate(employeeId, desiredDate);
    }
}
