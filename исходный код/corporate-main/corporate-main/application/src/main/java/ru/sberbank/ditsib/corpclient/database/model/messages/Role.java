package ru.sberbank.ditsib.corpclient.database.model.messages;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "corporate", name = "messages_roles")
@Getter
@Setter
public class Role {
    
    @Id
    private String code;
    
    @Column
    private String name;
    
}
