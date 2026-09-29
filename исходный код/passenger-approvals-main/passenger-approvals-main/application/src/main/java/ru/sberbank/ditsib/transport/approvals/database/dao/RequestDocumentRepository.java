package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;

import java.util.Set;
import java.util.UUID;

/**
 * Repository of request document
 */
public interface RequestDocumentRepository extends JpaRepository<RequestDocument, UUID>  {
    /**
     * Получение всех документов заявки
     */
    Set<RequestDocument> findByRequestId(UUID requestId);
}
