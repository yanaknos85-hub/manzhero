package ru.sberbank.ditsib.transport.approvals.database.model.messages;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Table(schema = "approvals", name = "messages_geo_zone")
@Entity
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
