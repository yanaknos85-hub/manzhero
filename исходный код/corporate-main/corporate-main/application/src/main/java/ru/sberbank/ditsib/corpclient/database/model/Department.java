package ru.sberbank.ditsib.corpclient.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.validation.HasId;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity of department
 */
@Entity
@Table(schema = "corporate", name = "department")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(onlyExplicitlyIncluded = true)
public class Department implements HasId, HasHumanReadableId, HasOrganization, HasOrgStructureType, HasActiveStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    @JoinColumn(nullable = false, name = "organization_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Organization organization;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, name = "name")
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;
    
    @Column
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Department> children = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head")
    private Employee head;

    @Column
    private String location;
    
    @OneToMany(mappedBy = "department", orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ActiveStatus activeStatus = ActiveStatus.ACTIVE;
    
    /**
     * Тип подразделения
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "org_structure_type")
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private OrgStructureType orgStructureType = OrgStructureType.EXTERNAL;

    /**
     * Время последнего обновления
     */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;
    
    @Column(name = "level_code")
    private Integer levelCode;
    
    @Column(name = "level_name")
    private String levelName;

    @Column(name = "sync_id")
    private String syncId;
    
    @Column
    private UUID geozone;

    @Builder.Default
    @Column(name = "is_handmade")
    private boolean isHandmade = false;

    @Builder.Default
    @Column(name = "filialFlag")
    private Boolean filialFlag = false;

    public void addEmployee(@NonNull Employee employee) {
        getEmployees().add(employee);
        employee.setDepartment(this);
    }
}
