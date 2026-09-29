package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Сущность делегата.
 */
@Entity
@Table(schema = "approvals", name = "corp_delegate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Delegate {
    
    @Id
    private UUID id;
    
    @Column(name = "delegate_id")
    private UUID delegateId;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "transport_type")
    private String transportType;
    
    @Column(name = "supervisor_id")
    private UUID supervisorId;
    
}
