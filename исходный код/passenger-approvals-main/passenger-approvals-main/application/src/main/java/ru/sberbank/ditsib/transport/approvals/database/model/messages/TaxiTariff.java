package ru.sberbank.ditsib.transport.approvals.database.model.messages;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Сущность тарифа такси
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "approvals", name = "taxi_tariff_message")
public class TaxiTariff {
    
    /** идентификатор */
    @Id
    private UUID id;
    
    /** человекочитаемый идентификатор */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /** ID геозоны */
    @Column(name = "region_id")
    private UUID regionId;
    
    /** вид транспортной услуги */
    @Column(name = "service_type")
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType;
    
    /** ID корп.клиента */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /** Тип транспорта */
    @Column(name = "transport_type")
    private String transportType;
    
    /** ID договора */
    @Column(name = "contract_id")
    private UUID contractId;
    
    /** Класс такси */
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
}
