package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория документов сотрудников")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Sql(scripts = "classpath:db/document-validate-test-schema.sql")
class EmployeeDocumentRepositoryTest extends SharedData {

    @Autowired
    private EmployeeDocumentRepository repository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DocumentType driverLicType;
    private DocumentType osagoType;
    private DocumentType passportTsType;
    private UUID employeeId;
    private UUID carId;

    @BeforeEach
    void setup() {
        // Сохраняем типы документов
        driverLicType = new DocumentType();
        driverLicType.setDocumentCode(DocumentCode.DRIVER_LIC);
        driverLicType.setName("Водительское удостоверение");
        driverLicType = documentTypeRepository.save(driverLicType);

        osagoType = new DocumentType();
        osagoType.setDocumentCode(DocumentCode.OSAGO);
        osagoType.setName("Полис ОСАГО");
        osagoType = documentTypeRepository.save(osagoType);

        passportTsType = new DocumentType();
        passportTsType.setDocumentCode(DocumentCode.PASSPORT_TS);
        passportTsType.setName("ПТС");
        passportTsType = documentTypeRepository.save(passportTsType);

        // Генерируем идентификаторы
        employeeId = UUID.randomUUID();
        carId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Тест hasEmployeeAccessToCar - доступ есть")
    void test_hasEmployeeAccessToCar_exists() {
        var driverLic = createEmployeeDocument(driverLicType, true);
        var osago = createEmployeeDocument(osagoType, true);
        var passportTs = createEmployeeDocument(passportTsType, true);
        repository.save(driverLic);
        repository.save(osago);
        repository.save(passportTs);

        var hasAccess = repository.existsByIdEmployeeIdAndCarId(employeeId, carId);

        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Тест hasEmployeeAccessToCar - доступа нет")
    void test_hasEmployeeAccessToCar_notExists() {
        boolean hasAccess = repository.existsByIdEmployeeIdAndCarId(employeeId, carId);

        assertThat(hasAccess).isFalse();
    }

    @Test
    @DisplayName("Тест findValidationDocuments - все документы валидны")
    void test_findValidationDocuments_allValid() {
        var driverLic = createEmployeeDocument(driverLicType, true);
        var osago = createEmployeeDocument(osagoType, true);

        repository.save(driverLic);
        repository.save(osago);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var documents = repository.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("Тест findValidationDocuments - ВУ недействительно")
    void test_findValidationDocuments_licenseExpired() {
        var driverLic = createEmployeeDocument(driverLicType, false);
        var osago = createEmployeeDocument(osagoType, true);

        repository.save(driverLic);
        repository.save(osago);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var documents = repository.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(1);
        assertThat(documents.getFirst().getDocumentType()).isEqualTo(osagoType);
    }

    @Test
    @DisplayName("Тест findValidationDocuments - ОСАГО недействительно")
    void test_findValidationDocuments_osagoExpired() {
        var driverLic = createEmployeeDocument(driverLicType, true);
        var osago = createEmployeeDocument(osagoType, false);

        repository.save(driverLic);
        repository.save(osago);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var documents = repository.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().hasSize(1);
        assertThat(documents.getFirst().getDocumentType()).isEqualTo(driverLicType);
    }

    @Test
    @DisplayName("Тест findValidationDocuments - оба документа недействительны")
    void test_findValidationDocuments_bothExpired() {
        var driverLic = createEmployeeDocument(driverLicType, false);
        var osago = createEmployeeDocument(osagoType, false);

        repository.save(driverLic);
        repository.save(osago);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var documents = repository.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Тест findValidationDocuments - документов нет")
    void test_findValidationDocuments_noDocuments() {
        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var documents = repository.findValidationDocuments(employeeId, carId, desiredDate);

        assertThat(documents).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Тест isDriverLicenseValidForDate - ВУ активно")
    void test_isDriverLicenseValidForDate_exists() {
        var driverLic = createEmployeeDocument(driverLicType, true);
        repository.save(driverLic);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var isValid = repository.isDriverLicenseValidForDate(employeeId, desiredDate);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Тест isDriverLicenseValidForDate - несколько активных ВУ")
    void test_isDriverLicenseValidForDate_manyExists() {
        var driverLic1 = createEmployeeDocument(driverLicType, true);
        var driverLic2 = createEmployeeDocument(driverLicType, true);
        repository.saveAll(List.of(driverLic1,driverLic2));

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var isValid = repository.isDriverLicenseValidForDate(employeeId, desiredDate);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Тест isDriverLicenseValidForDate - ВУ недействительно (будущее)")
    void test_isDriverLicenseValidForDate_futureNotValid() {
        var driverLic = createEmployeeDocument(driverLicType, false);
        repository.save(driverLic);

        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var isValid = repository.isDriverLicenseValidForDate(employeeId, desiredDate);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Тест isDriverLicenseValidForDate - документов нет")
    void test_isDriverLicenseValidForDate_noDocuments() {
        var desiredDate = LocalDateTime.now().plusDays(1).atZone(ZoneOffset.UTC).toLocalDateTime();

        var isValid = repository.isDriverLicenseValidForDate(employeeId, desiredDate);

        assertThat(isValid).isFalse();
    }

    private EmployeeDocument createEmployeeDocument(DocumentType type, boolean valid) {
        var now = LocalDateTime.now();
        var doc = new EmployeeDocument();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType(type);
        doc.setEmployeeId(employeeId);
        doc.setCarId(carId);
        doc.setFileName("test.pdf");
        doc.setFileSize(1024);
        doc.setFileFormat("PDF");
        doc.setCreationTime(now);
        doc.setCreationUser(UUID.randomUUID());
        doc.setIssueDateDocument(now);

        if (valid) {
            doc.setStartTimeDocument(now.minusDays(1));
            doc.setFinalTimeDocument(now.plusYears(1));
        } else {
            doc.setStartTimeDocument(now.plusDays(10));
            doc.setFinalTimeDocument(now.plusYears(2));
        }

        return doc;
    }
}
