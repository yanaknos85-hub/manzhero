package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;

import java.util.*;

/**
 * Entity of cargo type.
 */
@Entity
@Table(schema = "corporate", name = "cargo_type")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CargoType implements HasId {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    @Column
    private CargoTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column
    private CargoCategoryEnum category;

    @Column
    private double length;

    @Column
    private double width;

    @Column
    private double height;

    @Column
    private double weight;

    @Column
    private double volume;

    @Column
    private boolean active;

    /**
     * Организация
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Builder.Default
    @Column
    @OneToMany(mappedBy = "cargoType", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CargoGroup> cargoGroups = new ArrayList<>();;

}
