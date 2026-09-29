package ru.sberbank.ditsib.corpclient.database.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Table(schema = "corporate", name = "messages_geo_zone")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoZone {
    
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "code")
    private String code;
    
    @Column(name = "parent_id")
    private UUID parentId;
    
}
