package ru.sber.ditsib.transport.humanreadableid;

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
@Table
@Getter
@Setter
public class CompanySQRequest extends BaseCompanySQ {
}