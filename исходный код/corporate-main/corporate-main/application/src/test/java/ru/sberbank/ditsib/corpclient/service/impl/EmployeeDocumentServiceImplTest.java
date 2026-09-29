package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeDocumentRepository;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sberbank.ditsib.corpclient.service.EmployeeDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса документов сотрудников")
class EmployeeDocumentServiceImplTest {

    private final EmployeeDocumentRepository employeeDocumentRepository = mock(EmployeeDocumentRepository.class);

    private final EmployeeDocumentService employeeDocumentService = new EmployeeDocumentServiceImpl(employeeDocumentRepository);

    private UUID employeeId;
    private UUID carId;

    @BeforeEach
    void init() {
        employeeId = UUID.randomUUID();
        carId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Тест hasEmployeeAccessToCar - доступ есть")
    void test_hasEmployeeAccessToCar_exists() {
        when(employeeDocumentRepository.existsByIdEmployeeIdAndCarId(any(), any())).thenReturn(true);

        var hasAccess = employeeDocumentService.hasEmployeeAccessToCar(employeeId, carId);

        assertThat(hasAccess).isTrue();
        verify(employeeDocumentRepository).existsByIdEmployeeIdAndCarId(employeeId, carId);
    }

    @Test
    @DisplayName("Тест hasEmployeeAccessToCar - доступа нет")
    void test_hasEmployeeAccessToCar_notExists() {
        when(employeeDocumentRepository.existsByIdEmployeeIdAndCarId(any(), any())).thenReturn(false);

        var hasAccess = employeeDocumentService.hasEmployeeAccessToCar(employeeId, carId);

        assertThat(hasAccess).isFalse();
        verify(employeeDocumentRepository).existsByIdEmployeeIdAndCarId(employeeId, carId);
    }

    @Test
    @DisplayName("Тест findValidationDocuments - все документы валидны")
    void test_findValidationDocuments_allValid() {
        var driverLic = createEmployeeDocument(DocumentCode.DRIVER_LIC);
        var osago = createEmployeeDocument(DocumentCode.OSAGO);

        when(employeeDocumentRepository.findValidationDocuments(any(), any(), any())).thenReturn(List.of(driverLic, osago));

        var desiredDate = LocalDateTime.now().plusDays(1);
        var documents = employeeDocumentService.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(2);

        var captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(employeeDocumentRepository).findValidationDocuments(eq(employeeId), eq(carId), captor.capture());
        assertThat(captor.getValue()).isEqualTo(desiredDate);
    }

    @Test
    @DisplayName("Тест findValidationDocuments - ВУ недействительно")
    void test_findValidationDocuments_licenseExpired() {
        var osago = createEmployeeDocument(DocumentCode.OSAGO);

        when(employeeDocumentRepository.findValidationDocuments(any(), any(), any())).thenReturn(List.of(osago));

        var desiredDate = LocalDateTime.now().plusDays(10);
        var documents = employeeDocumentService.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(1);
        assertThat(documents.getFirst().getDocumentType()).isEqualTo(osago.getDocumentType());
    }

    @Test
    @DisplayName("Тест findValidationDocuments - ОСАГО недействительно")
    void test_findValidationDocuments_osagoExpired() {
        var driverLic = createEmployeeDocument(DocumentCode.DRIVER_LIC);

        when(employeeDocumentRepository.findValidationDocuments(any(), any(), any())).thenReturn(List.of(driverLic));

        var desiredDate = LocalDateTime.now().plusDays(10);
        var documents = employeeDocumentService.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(1);
        assertThat(documents.getFirst().getDocumentType()).isEqualTo(driverLic.getDocumentType());
    }

    @Test
    @DisplayName("Тест findValidationDocuments - оба документа недействительны")
    void test_findValidationDocuments_bothExpired() {
        when(employeeDocumentRepository.findValidationDocuments(any(), any(), any())).thenReturn(List.of());

        var desiredDate = LocalDateTime.now().plusDays(10);
        var documents = employeeDocumentService.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Тест findValidationDocuments - документов нет")
    void test_findValidationDocuments_noDocuments() {
        when(employeeDocumentRepository.findValidationDocuments(any(), any(), any())).thenReturn(List.of());

        var desiredDate = LocalDateTime.now().plusDays(1);
        var documents = employeeDocumentService.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Тест hasEmployeeActiveDriverLicense - ВУ активно")
    void test_hasEmployeeActiveDriverLicense_exists() {
        var desiredDate = LocalDateTime.now().plusDays(1);

        when(employeeDocumentRepository.isDriverLicenseValidForDate(any(), any())).thenReturn(true);

        var hasLicense = employeeDocumentService.hasEmployeeActiveDriverLicense(employeeId, desiredDate);

        assertThat(hasLicense).isTrue();
        verify(employeeDocumentRepository).isDriverLicenseValidForDate(employeeId, desiredDate);
    }

    @Test
    @DisplayName("Тест hasEmployeeActiveDriverLicense - ВУ неактивно")
    void test_hasEmployeeActiveDriverLicense_notExists() {
        var desiredDate = LocalDateTime.now().plusDays(1);

        when(employeeDocumentRepository.isDriverLicenseValidForDate(any(), any())).thenReturn(false);

        var hasLicense = employeeDocumentService.hasEmployeeActiveDriverLicense(employeeId, desiredDate);

        assertThat(hasLicense).isFalse();
        verify(employeeDocumentRepository).isDriverLicenseValidForDate(employeeId, desiredDate);
    }

    private EmployeeDocument createEmployeeDocument(DocumentCode code) {
        var documentType = new DocumentType();
        documentType.setDocumentCode(code);
        documentType.setName(code.name());

        var doc = new EmployeeDocument();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType(documentType);
        doc.setEmployeeId(employeeId);
        doc.setCarId(carId);
        doc.setFileName("test.pdf");
        doc.setFileSize(1024);
        doc.setFileFormat("PDF");
        doc.setCreationTime(LocalDateTime.now());
        doc.setCreationUser(UUID.randomUUID());
        doc.setIssueDateDocument(LocalDateTime.now());
        doc.setStartTimeDocument(LocalDateTime.now().minusDays(1));
        doc.setFinalTimeDocument(LocalDateTime.now().plusYears(1));

        return doc;
    }
}
