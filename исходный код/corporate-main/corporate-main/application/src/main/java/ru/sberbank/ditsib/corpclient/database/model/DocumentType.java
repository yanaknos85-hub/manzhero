package ru.sberbank.ditsib.corpclient.database.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "corporate", name = "document_type")
@Data
public class DocumentType {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "document_code")
    private DocumentCode documentCode;

    @Column(nullable = false)
    private String name;

    public DocumentType(DocumentCode documentCode, String name) {
        this.documentCode = documentCode;
        this.name = name;
    }

    public DocumentType() {

    }
}