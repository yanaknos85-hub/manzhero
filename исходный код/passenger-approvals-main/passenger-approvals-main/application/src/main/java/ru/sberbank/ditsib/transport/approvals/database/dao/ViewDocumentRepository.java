package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;

import java.util.UUID;

/**
 * Repository of request document view
 */

public interface ViewDocumentRepository extends JpaRepository<ViewDocument, UUID> {
}
