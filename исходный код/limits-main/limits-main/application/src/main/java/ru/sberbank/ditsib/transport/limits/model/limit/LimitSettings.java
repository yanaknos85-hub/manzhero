package ru.sberbank.ditsib.transport.limits.model.limit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;

import jakarta.persistence.*;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "limits", name = "limit_settings")
@Data
public class LimitSettings {
    
    /**
     * Name
     */
    @Id
    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SettingsNames name;
    
    /**
     * Value
     */
    @Column(name = "value", nullable = false)
    private String value;
}
