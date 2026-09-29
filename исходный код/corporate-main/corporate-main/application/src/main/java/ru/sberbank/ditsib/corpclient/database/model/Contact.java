package ru.sberbank.ditsib.corpclient.database.model;

import lombok.Getter;
import lombok.Setter;


import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(schema = "corporate", name = "contact")
@Getter
@Setter
public class Contact {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "type", nullable = false)
    private ContactType contactType;

    @Column(nullable = false)
    private String value;

}
