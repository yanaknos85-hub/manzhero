package ru.sberbank.ditsib.corpclient.database.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entity of status personal car.
 */
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "corporate",name = "active_personal_car")
public class ActivePersonalCar {

    /**
     * Identifier.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Owner.
     */
    @JoinColumn(name = "owner_id", unique = true)
    @ManyToOne
    private Employee owner;

    /**
     * Link to personal car.
     */
    @JoinColumn(name = "car_id")
    @OneToOne
    private PersonalCar personalCar;
}
