package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity of attribute.
 */
@Entity
@Table(schema = "corporate", name = "attribute")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Attribute implements HasId {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AttributeStatus status = AttributeStatus.ACTIVE;

    @ManyToMany(mappedBy = "attributes")
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

}
