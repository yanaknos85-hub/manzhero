package ru.sberbank.ditsib.corpclient.database.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.constants.PersonalTransportType;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Entity of personal car
 */
@Entity
@Table(schema = "corporate", name = "personal_auto")
@SQLRestriction("active=true")
@SQLDelete(sql = "UPDATE corporate.personal_auto SET active = false WHERE id = ?")
@Getter
@Setter
public class PersonalCar implements HasId {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private PersonalTransportType transportType = PersonalTransportType.CAR;

    @Column(name = "brand_name")
    private String brandName;

    @Column
    private String color;

    @Column
    private String model;

    @Column(name = "reg_number", unique = true, nullable = false)
    @NotNull(message = "Set registration number")
    private String registrationNumber;

    @Column(name = "reg_cert", unique = true)
    private String registrationCertificate;

    @Column(name = "engine_volume")
    private int engineVolume = 1000;
    
    @Column(name = "insurance_number", unique = true)
    private String insuranceNumber;

    @ManyToOne
    @JoinColumn(name = "corporate_user_id", nullable = false)
    @NotNull(message = "Set associated user")
    private Employee employee;


    @Enumerated(EnumType.STRING)
    @Column(name = "owner_info")
    private PersonalCarOwnerInfo ownerInfo = PersonalCarOwnerInfo.USER;

    @Column(name = "passenger_seats_count")
    private short passengerSeatsCount = 0;

    @OneToOne(mappedBy = "personalCar", orphanRemoval = true)
    private ActivePersonalCar activePersonalCar;

    @Column(name = "pers_data_accept")
    private String persDataAccept;

    @Column(name = "active")
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonalCar)) {
            return false;
        }
        PersonalCar other = (PersonalCar) o;
        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
