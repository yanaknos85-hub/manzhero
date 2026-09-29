package ru.sberbank.ditsib.transport.limits.model.basic;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "limits", name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class Employee {
    
    @Id
    private UUID id;
    
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column
    private String patronymic;
    
    @Column(name = "personnel_number")
    private String personnelNumber;

    @Column(name = "email")
    private String email;
    
    @Column(name = "user_id", unique = true)
    private UUID userId;
    
    @Column(name = "department_id")
    private UUID departmentId;
    
    @Column(name = "supervisor_id")
    private UUID supervisorId;
    
    @Column(name = "position_id")
    private UUID positionId;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = false;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
