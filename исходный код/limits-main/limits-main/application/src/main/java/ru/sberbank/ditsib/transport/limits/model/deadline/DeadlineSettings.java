package ru.sberbank.ditsib.transport.limits.model.deadline;

import lombok.*;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.descriptor.jdbc.SmallIntJdbcType;
import ru.sberbank.ditsib.transport.constants.LimitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Сущность - настройки контрольных сроков для организации, получаемые из сообщения */
@Entity
@Table(schema = "limits", name = "deadline_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineSettings {
    
    /** ID настройки */
    @Id
    private UUID id;
    
    /** ID корп.клиента. Одна настройка для одного корп.клиента */
    @Column(name = "organization_id")
    private UUID organizationId;

    @JavaType(StringJavaType.class)
    @Column(name = "employee_limit_chrono_unit", columnDefinition = "varchar (Types#VARCHAR)")
    private ChronoUnit employeeLimitChronoUnit;

    @JdbcType(SmallIntJdbcType.class)
    @Column(name = "employee_limit_value", columnDefinition = "int2 (Types#SMALLINT)")
    private Integer employeeLimitValue;

    @JavaType(StringJavaType.class)
    @Column(name = "department_limit_chrono_unit", columnDefinition = "varchar (Types#VARCHAR)")
    private ChronoUnit departmentLimitChronoUnit;

    @JdbcType(SmallIntJdbcType.class)
    @Column(name = "department_limit_value", columnDefinition = "int2 (Types#SMALLINT)")
    private Integer departmentLimitValue;
    
    /**
     * Вернуть контрольный срок для типа лимита
     * @param type - тип лимита
     */
    public Duration getDuration(LimitType type) {
        if (type == LimitType.DEPARTMENT) {
            return Duration.of(departmentLimitValue, departmentLimitChronoUnit);
        }
        return Duration.of(employeeLimitValue, employeeLimitChronoUnit);
    }

}
