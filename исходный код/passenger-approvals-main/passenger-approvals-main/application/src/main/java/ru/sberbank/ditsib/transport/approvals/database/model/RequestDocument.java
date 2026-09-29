package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Документ заявки
 **/
@Entity
@Table(schema = "approvals", name = "request_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDocument {
    
    /**
     * документ. Является первичным ключем
     */
    @Id
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    /**
     * заявка
     */
    @Column(name = "request_id", nullable = false)
    private UUID requestId;
    
    /**
     * сотрудник, загрузивший документ
     */
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
    
/*
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "approvals", name = "view_document", joinColumns = @JoinColumn(name = "document_id"))
    @AttributeOverrides({
          @AttributeOverride(name = "employeeId", column = @Column(name = "employee_id")),
          @AttributeOverride(name = "dateTime", column = @Column(name = "date_time"))
    })
*/
    /**
     * просмотр документа
     */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id")
    private Set<ViewDocument> views = new HashSet<>();
}
