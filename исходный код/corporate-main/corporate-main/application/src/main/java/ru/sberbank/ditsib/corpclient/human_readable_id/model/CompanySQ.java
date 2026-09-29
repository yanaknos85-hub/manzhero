package ru.sberbank.ditsib.corpclient.human_readable_id.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Entity of sequence
 */

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(schema = "corporate", name = "company_sq")
@Getter
@Setter
public class CompanySQ extends BaseCompanySQ implements Serializable {
    private static final long serialVersionUID = 1L;
    
}