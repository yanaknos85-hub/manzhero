package ru.sberbank.ditsib.transport.limits.model.basic;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(schema = "limits", name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Department {
    
    @Id
    private UUID id;
    
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(unique = true)
    private String code;
    
    @Column(name = "department_name")
    private String departmentName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_id")
    private Employee departmentHead;
    
    @Column(name = "parent_id")
    private UUID parentId;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = false;
}
