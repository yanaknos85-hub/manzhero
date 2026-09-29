package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fraud", schema = "approvals")
@Getter
@Setter
public class FraudData {
    @Id
    @GeneratedValue
    private UUID id;

    private String type;

    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id")
    @NotNull
    private Approval approval;

    /**
     * Комментарий антифрода
     */
    private String comment;
}
