package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;


import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Entity of cargo delivery time.
 */
@Entity
@Table(schema = "corporate", name = "cargo_delivery_time")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CargoDeliveryTime implements HasId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String label;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CargoDeliveryTimeUrgency urgency;

    @Column(name = "dist_start")
    @NotNull
    private Integer start;

    @Column(name = "dist_end")
    @NotNull
    private Integer end;

    @Column(name = "default_value")
    @NotNull
    private Integer defaultValue;

    @NotNull
    private Integer value;

    @Builder.Default
    private boolean active = true;
}
