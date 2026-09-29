package ru.sberbank.ditsib.corpclient.database.model;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;

import java.util.UUID;

/**
 * Entity of cargo group.
 */
@Entity
@Table(schema = "corporate", name = "cargo_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CargoGroup implements HasId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cargo_type_id", nullable = false)
    private CargoType cargoType;

    @Column
    private int count;

}
