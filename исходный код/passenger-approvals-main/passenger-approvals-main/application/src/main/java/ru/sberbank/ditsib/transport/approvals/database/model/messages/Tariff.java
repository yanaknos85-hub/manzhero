package ru.sberbank.ditsib.transport.approvals.database.model.messages;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Cущность тарифа каршеринга, получаемая из сообщения
 */
@Entity
@Table(schema = "approvals", name = "tariff_message")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {
    
    /** Идентификатор */
    @Id
    private UUID id;
    
    /** Человекочитаемый идентификатор */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /** Организация владелец тарифа */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /** ID геозоны */
    @Column(name = "region_id")
    private UUID regionId;
    
    /** Тип транспорта */
    @Column(name = "transport_type")
    private String transportType;
    
    /** Контракт */
    @Column(name = "contract_id")
    private UUID contractId;
}
