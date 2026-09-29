package ru.sberbank.ditsib.transport.limits.human_readable_id.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity of sequence
 */

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(schema = "limits", name = "company_sq")
@Getter
@Setter
public class CompanySQLimits extends BaseCompanySQ {
}