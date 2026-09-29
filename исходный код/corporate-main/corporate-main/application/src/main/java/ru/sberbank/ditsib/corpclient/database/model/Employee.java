package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.descriptor.jdbc.DateJdbcType;
import org.springframework.data.domain.Persistable;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

/**
 * Entity of employee
 */
@Entity
@Table(schema = "corporate", name = "employee")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Employee implements HasId, Persistable<UUID>, HasHumanReadableId, HasOrganization, HasOrgStructureType, HasActiveStatus {

    @Id
    private UUID id;

    @Transient
    private boolean isNew;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String patronymic;

    @Column(name = "personnel_number")
    @ToString.Include
    private String personnelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @OneToMany(mappedBy = "supervisor")
    private List<DelegateRecord> delegateRecords;

    @ElementCollection(targetClass = TransportTypeEnum.class)
    @CollectionTable(schema = "corporate", name = "employee_transport_type",
            joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<TransportTypeEnum> availableTransportTypes = new HashSet<>();

    @Column(name = "mobile_phone")
    private String mobilePhone;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private String email;

    @Column(name = "external_email")
    private String externalEmail;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @OneToMany(mappedBy = "supervisor")
    @Builder.Default
    private List<Employee> slaves = new ArrayList<>();

    @OneToMany(mappedBy = "employee", orphanRemoval = true)
    @Builder.Default
    private Set<PersonalCar> personalCars = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ActiveStatus activeStatus = ActiveStatus.ACTIVE;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "employee_attribute",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "employee_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "attribute_id", nullable = false))
    @Builder.Default
    private Set<Attribute> attributes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Gender gender;

    @JdbcType(DateJdbcType.class)
    @Column(name = "fire_date")
    private LocalDate fireDate;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "room")
    private String room;

    @Column(name = "consent")
    private boolean consent;

    @Column(name = "marriage_certificate_id")
    private String marriageCertificateId;

    /**
     * Место возникновения затрат
     */
    @Column(name = "cost_center")
    private String costCenter;

    /**
     * Тип сотрудника
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

    /**
     * Тип разъездного характера сотрудника
     */
    @Column(name = "itinerant_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ItinerantType itinerantType;

    @ElementCollection
    @CollectionTable(schema = "corporate_approvals", name = "approvals",
            joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "action_id")
    @Builder.Default
    private Set<UUID> actions = new HashSet<>();

    @OneToMany(mappedBy = "delegate")
    @Builder.Default
    private List<DelegateRecord> delegatedBy = new ArrayList<>();

    @OneToMany(mappedBy = "supervisor")
    @Builder.Default
    private List<DelegateRecord> supervisorOf = new ArrayList<>();

    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Employee other) {
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
