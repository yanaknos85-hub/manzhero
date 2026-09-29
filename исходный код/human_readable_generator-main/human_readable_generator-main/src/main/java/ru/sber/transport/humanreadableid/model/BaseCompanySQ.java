package ru.sber.transport.humanreadableid.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Базовый класс для последовательностей.
 */
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class BaseCompanySQ {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    /**
     * Prefix
     */
    @Column(nullable = false)
    @Size(min = 2, max = 2)
    protected String prefix;
    /**
     * Organization digit id
     */
    @Column(name = "orgdigitid", nullable = false, columnDefinition = "numeric")
    private Long orgDigitId;
    /**
     * Sequence value
     */
    @Column(nullable = false, columnDefinition = "numeric")
    @Builder.Default
    private Long sq = 1L;
}
