package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;
import java.util.UUID;
    
@Entity
@Table(schema = "corporate", name = "transport_org")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransportOrg {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(name = "transport_type")
    private String transportType;
}
    

