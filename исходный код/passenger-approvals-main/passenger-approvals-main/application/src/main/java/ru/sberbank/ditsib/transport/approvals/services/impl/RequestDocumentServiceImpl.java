package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.approvals.database.dao.RequestDocumentRepository;
import ru.sberbank.ditsib.transport.approvals.database.dao.ViewDocumentRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;
import ru.sberbank.ditsib.transport.approvals.services.RequestDocumentService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of request document service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class RequestDocumentServiceImpl implements RequestDocumentService {
    
    private final RequestDocumentRepository documentRepository;
    
    private final ViewDocumentRepository viewRepository;
    
    @Override
    public void delete(UUID documentId) {
        documentRepository.findById(documentId).ifPresent(document -> {
            // Приходится вручную удалять views, так как hibernate при каскадном удалении сначала обнуляет
            // document_id, который в базе not null => ошибка
            document.getViews().forEach(viewRepository::delete);
            viewRepository.flush();// Без этого генерируется update на зануление поля document_id
            documentRepository.delete(document);
        });
    }
    
    @Override
    public void save(RequestDocument document) {
        documentRepository.save(document);
    }
    
    @Override
    public void documentViewedBy(ViewDocument viewDocument) {
        viewRepository.save(viewDocument);
    }
    
    @Override
    public Set<RequestDocument> findDocuments(UUID requestId) {
        return documentRepository.findByRequestId(requestId);
    }
    
    /**
     * Проверка все ли документы были просмотрены согласующим
     */
    @Override
    public void checkViews(UUID approvedByEmployeeId, UUID requestId) {
        findDocuments(requestId).forEach(document -> checkDocument(approvedByEmployeeId, document));
    }
    
    /**
     * Проверка наличия просмотра документа согласующим
     */
    private void checkDocument(UUID approvedByEmployeeId, RequestDocument document) {
        // Если согласующий это сотрудник, загрузивший документ, считаем, что он знает что загрузил, поэтому
        // просматривать не обязательно
        if (approvedByEmployeeId.equals(document.getEmployeeId())) {
            return;
        }
        // проверяем просматривал ли документ согласующий
        if (document.getViews().stream()
                    .anyMatch(doc -> doc.getEmployeeId().equals(approvedByEmployeeId))) {
            return;
        }
        throw new IllegalStateResponseException("Please view all request documents");
    }
    
}
