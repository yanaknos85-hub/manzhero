package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Информация о просмотре документа
 **/
@Entity
@Table(schema = "approvals", name = "view_document")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewDocument {
    
    @Id
    private UUID id;

    /**
     * документ
     */
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    /**
     * Сотрудник, просмотревший документ
     */
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
    
    /**
     * Дата и время просмотра документа
     */
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;
}
