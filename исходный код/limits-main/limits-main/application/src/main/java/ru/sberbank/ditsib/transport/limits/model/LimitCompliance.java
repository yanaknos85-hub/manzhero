package ru.sberbank.ditsib.transport.limits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "limits", name = "limit_compliance")
public class LimitCompliance {

    @Id
    @Column(name = "limit_id", nullable = false)
    private UUID limitId;

    @Column(name = "reserve_id", nullable = false)
    private UUID reserveId;
}
