package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория видов документов")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
public class DocumentTypeRepositoryTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Test
    @DisplayName("Тест сохранения вида документа")
    void documentTypeSaveTest() {
        DocumentType documentType1 = new DocumentType();
        documentType1.setDocumentCode(DocumentCode.DRIVER_LIC);
        documentType1.setName("Водительское удостоверение");
        documentTypeRepository.save(documentType1);

        DocumentType documentType2 = new DocumentType();
        documentType2.setDocumentCode(DocumentCode.OSAGO);
        documentType2.setName("Полис ОСАГО");
        documentTypeRepository.save(documentType2);

        var documentTypes = documentTypeRepository.findAll();

        assertEquals(2, documentTypes.size());
    }

    @Test
    @DisplayName("Тест получения вида документа по коду")
    void documentTypeGetByIdTest() {
        DocumentType documentType1 = new DocumentType();
        documentType1.setDocumentCode(DocumentCode.DRIVER_LIC);
        documentType1.setName("Водительское удостоверение");
        documentTypeRepository.save(documentType1);

        DocumentType documentType2 = new DocumentType();
        documentType2.setDocumentCode(DocumentCode.OSAGO);
        documentType2.setName("Полис ОСАГО");
        documentTypeRepository.save(documentType2);

        var documentType = documentTypeRepository.getReferenceById(DocumentCode.DRIVER_LIC);

        assertEquals(documentType.getName(), "Водительское удостоверение");
    }
}