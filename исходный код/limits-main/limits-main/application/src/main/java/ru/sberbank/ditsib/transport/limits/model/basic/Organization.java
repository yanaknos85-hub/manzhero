package ru.sberbank.ditsib.transport.limits.model.basic;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.NumericJdbcType;

import java.util.UUID;

@Entity
@Table(schema = "limits", name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {
    
    @Id
    private UUID id;

    @JdbcType(NumericJdbcType.class)
    @Column(name = "digit_id", columnDefinition = "numeric (Types#NUMERIC)")
    private Long digitId;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = false;
}
