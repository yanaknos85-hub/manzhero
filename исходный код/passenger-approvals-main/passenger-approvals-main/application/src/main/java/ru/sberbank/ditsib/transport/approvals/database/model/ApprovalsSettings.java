package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Базовая сущность настроек согласования
 */
@Entity
@Getter
@Setter
@Table(schema = "approvals", name = "approvals_settings",
       uniqueConstraints = { @UniqueConstraint(columnNames = { "organization_id", "transport_type" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "setting_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class ApprovalsSettings {
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
    
    /**
     * Ссылка на корп.клиента
     */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;
    
    /**
     * Необходимость этапа согласования
     */
    @Builder.Default
    @Column(name = "approval_active")
    private boolean approvalActive = true;
    
    /**
     * Сумма, не требующая согласования
     */
    @Builder.Default
    @Column(name = "min_cost_to_be_approved")
    private long minCostToBeApproved = 0;
    
    /**
     * Тип транспорта
     */
    @Column(name = "transport_type")
    private String transportType;
    
    /**
     * Список элементов настроек по региону и целям поездки. Опциональная настройка
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @CollectionTable(
            schema = "approvals",
            name = "purpose_and_region_items",
            joinColumns = @JoinColumn(name = "setting_id"))
    private List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems = new ArrayList<>();
    
    
}
