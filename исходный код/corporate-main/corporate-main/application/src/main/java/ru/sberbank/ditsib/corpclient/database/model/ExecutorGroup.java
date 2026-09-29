package ru.sberbank.ditsib.corpclient.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.util.ContextHelper;
import ru.sberbank.ditsib.transport.validation.HasId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Entity of executor group
 */
@Entity
@Table(schema = "corporate", name = "executor_group")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutorGroup implements HasId, HasHumanReadableId {

    @Id
    private UUID id;

    @Column(name = "humanreadableid", nullable = false, length = 16)
    private String humanReadableId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "service", nullable = false, length = 50)
    private String service;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "service_level", length = 50)
    private String serviceLevel;

    @Column(name = "author_id", length = 50)
    private UUID authorId;

    @Column(name = "user_id", length = 50)
    private UUID userId;

    @Column(name = "organization_id", length = 50)
    private UUID organizationId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "executor_group_executor",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "executor_group_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private Set<Employee> executors = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "executor_group_organization",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "executor_group_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @Builder.Default
    private Set<Organization> organizations = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "executor_group_department",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "executor_group_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    @Builder.Default
    private Set<Department> departments = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "executor_group_customer",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "executor_group_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private Set<Employee> customers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "executor_group_geo_zone",
            schema = "corporate",
            joinColumns = @JoinColumn(name = "executor_group_id"),
            inverseJoinColumns = @JoinColumn(name = "geo_zone_id"))
    @Builder.Default
    private Set<GeoZone> geoZones = new HashSet<>();

    @Column(columnDefinition = "jsonb", name = "contractors")
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<UUID> contractors = new HashSet<>();

    @Column(length = 64)
    private String additionalFeature;

    @SuppressWarnings("unused")
    @PrePersist
    protected void prePersist() {
        setAuthorId(ContextHelper.getCurrentUser() == null ? null : UUID.fromString(ContextHelper.getCurrentUser()));
        setUserId(ContextHelper.getCurrentUser() == null ? null : UUID.fromString(ContextHelper.getCurrentUser()));
        setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        setUpdateTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
    }

    @PreUpdate
    protected void preUpdate() {
        setUserId(ContextHelper.getCurrentUser() == null ? null : UUID.fromString(ContextHelper.getCurrentUser()));
        setUpdateTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
    }
}
