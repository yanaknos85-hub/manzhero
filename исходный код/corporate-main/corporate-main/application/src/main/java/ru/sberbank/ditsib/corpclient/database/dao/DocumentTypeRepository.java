package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

@Repository
public interface DocumentTypeRepository extends JpaSpecificationExecutor<DocumentType>, JpaRepository<DocumentType, DocumentCode> {

}