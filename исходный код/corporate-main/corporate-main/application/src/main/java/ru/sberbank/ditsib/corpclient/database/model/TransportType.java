package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "corporate", name = "transport_type")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransportType {
    
    @Id
    private UUID id;
    
    @Column(unique = true)
    private String name;
    
}
