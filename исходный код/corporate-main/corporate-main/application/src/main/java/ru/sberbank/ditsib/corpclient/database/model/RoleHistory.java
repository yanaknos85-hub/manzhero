package ru.sberbank.ditsib.corpclient.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity истории назначения ролей сотруднику.
 */
@Entity
@Table(schema = "corporate", name = "role_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RoleHistory {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime actionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RoleActionType actionType;

    private String comment;

    @NotNull
    private UUID employeeId;

    @NotNull
    private String roleNames;
}
