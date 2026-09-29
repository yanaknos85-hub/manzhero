package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;

import java.util.Set;
import java.util.UUID;

public interface RequestDocumentService {

    /**
     * Delete document.
     */
    void delete(UUID documentId);

    /**
     * Save document.
     */
    void save(RequestDocument document);
    
    /**
     * Add information about view document
     */
    void documentViewedBy(ViewDocument viewDocument);
    
    /**
     * Получение всех документов заявки
     */
    Set<RequestDocument> findDocuments(UUID requestId);
    
    /**
     * Проверка просмотра согласующим документов заявки
     * @param approvedByEmployeeId согласующий
     */
    void checkViews(UUID approvedByEmployeeId, UUID requestId);
}
