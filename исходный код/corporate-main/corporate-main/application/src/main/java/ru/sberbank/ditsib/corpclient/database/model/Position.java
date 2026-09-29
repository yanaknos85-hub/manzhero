package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Entity of position
 */
@Entity
@Table(schema = "corporate", name = "position")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Position implements HasId, HasHumanReadableId, HasOrganization, HasOrgStructureType, HasActiveStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @ToString.Exclude
    @ElementCollection(targetClass = TaxiClass.class)
    @CollectionTable(schema = "corporate", name = "position_taxi_classes", joinColumns = @JoinColumn(name = "position_id"))
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<TaxiClass> availableClasses = new HashSet<>();

    @Column(name = "self_approved")
    @Builder.Default
    private boolean selfApproved = false;
    
    /**
     * Тип подразделения
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "org_structure_type")
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private OrgStructureType orgStructureType = OrgStructureType.EXTERNAL;
    
    /**
     * Время последнего обновления из ЕАСУП
     */
    @Builder.Default
    @Column(name = "update_time")
    private OffsetDateTime updateTime = OffsetDateTime.now();
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ActiveStatus activeStatus = ActiveStatus.ACTIVE;

    @ToString.Exclude
    @OneToMany(mappedBy = "position")
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Position other) {
            return id != null &&
                    id.equals(other.getId());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return 31;
    }
}
