package ru.sberbank.ditsib.transport.approvals.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.ditsib.transport.approvals.database.dao.RequestDocumentRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;
import ru.sberbank.ditsib.transport.approvals.services.RequestDocumentService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Проверка сервиса проверки документов")
public class CheckDocumentServiceTest {
    @Autowired
    private RequestDocumentRepository documentRepository = mock(RequestDocumentRepository.class);
    
    private RequestDocumentService documentService = new RequestDocumentServiceImpl(documentRepository, null);
    
//    @AfterEach
//    public void afterEach() {
//        documentRepository.deleteAll();
//        viewDocumentRepository.deleteAll();
//    }

    @Test
    @DisplayName("Успех. Нет документов")
    public void testEmptyDocuments() {
        UUID requestId = UUID.randomUUID();
        UUID employeeId1 = UUID.randomUUID();
        when(documentRepository.findByRequestId(requestId)).thenReturn(set());
        documentService.checkViews(employeeId1, requestId);
    }
    
    @Test
    @DisplayName("Успех. Согласующий сам загружал документ")
    public void testSelfDocuments() {
        UUID requestId = UUID.randomUUID();
        UUID employeeId1 = UUID.randomUUID();
        RequestDocument document1 = new RequestDocument();
        RequestDocument document2 = new RequestDocument();
        document1.setEmployeeId(employeeId1);
        document2.setEmployeeId(employeeId1);
        when(documentRepository.findByRequestId(requestId)).thenReturn(set(document1, document2));
        documentService.checkViews(employeeId1, requestId);
    }
    
    @Test
    @DisplayName("Успех. Документы просмотрены согласующим")
    public void testViewedDocuments() {
        UUID requestId = UUID.randomUUID();
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        RequestDocument document1 = new RequestDocument();
        RequestDocument document2 = new RequestDocument();
        document1.setEmployeeId(employeeId2);
        document2.setEmployeeId(employeeId2);
        document1.getViews().add(ViewDocument.builder().employeeId(employeeId1).build());
        document2.getViews().add(ViewDocument.builder().employeeId(employeeId1).build());
        when(documentRepository.findByRequestId(requestId)).thenReturn(set(document1, document2));
        documentService.checkViews(employeeId1, requestId);
    }
    
    @Test
    @DisplayName("Fail. Один документ просмотрен не согласующим")
    public void testFailOneDocumentViewedAnother() {
        UUID requestId = UUID.randomUUID();
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        RequestDocument document1 = new RequestDocument();
        RequestDocument document2 = new RequestDocument();
        document1.setEmployeeId(employeeId2);
        document2.setEmployeeId(employeeId2);
        document1.getViews().add(ViewDocument.builder().employeeId(employeeId2).build());
        document1.getViews().add(ViewDocument.builder().employeeId(employeeId1).build());
        document2.getViews().add(ViewDocument.builder().employeeId(employeeId2).build());
        when(documentRepository.findByRequestId(requestId)).thenReturn(set(document1, document2));
        assertThatThrownBy(() -> documentService.checkViews(employeeId1, requestId)).isInstanceOf(
                IllegalStateResponseException.class) ;
    }
    
    @Test
    @DisplayName("Fail. Один документ не просмотрен")
    public void testFailOneDocumentNOViewed() {
        UUID requestId = UUID.randomUUID();
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        RequestDocument document1 = new RequestDocument();
        RequestDocument document2 = new RequestDocument();
        document1.setEmployeeId(employeeId2);
        document2.setEmployeeId(employeeId2);
        document1.getViews().add(ViewDocument.builder().employeeId(employeeId1).build());
        when(documentRepository.findByRequestId(requestId)).thenReturn(set(document1, document2));
        assertThatThrownBy(() -> documentService.checkViews(employeeId1, requestId)).isInstanceOf(
                IllegalStateResponseException.class) ;
    }
    
    private Set<RequestDocument> set(RequestDocument... documents) {
        return new HashSet<>(Arrays.asList(documents));
    }
    
}
