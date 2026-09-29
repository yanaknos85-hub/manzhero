package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "limits", name = "limit_sharing_percents")
@Data
public class LimitSharingPercents {
    
    /**
     * Unique idetificator.
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Author of sharing
     */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    /**
     * Limit id
     */
    @ManyToOne
    @JoinColumn(name = "limit_id", nullable = false)
    private Limit limit;
    
    /**
     * Month 0
     */
    @Column(name = "january", nullable = false)
    private Integer january;
    
    /**
     * Month 1
     */
    @Column(name = "february", nullable = false)
    private Integer february;
    
    /**
     * Month 2
     */
    @Column(name = "march", nullable = false)
    private Integer march;
    
    /**
     * Month 3
     */
    @Column(name = "april", nullable = false)
    private Integer april;
    
    /**
     * Month 4
     */
    @Column(name = "may", nullable = false)
    private Integer may;
    
    /**
     * Month 5
     */
    @Column(name = "june", nullable = false)
    private Integer june;
    
    /**
     * Month 6
     */
    @Column(name = "july", nullable = false)
    private Integer july;
    
    /**
     * Month 7
     */
    @Column(name = "august", nullable = false)
    private Integer august;
    
    /**
     * Month 8
     */
    @Column(name = "september", nullable = false)
    private Integer september;
    
    /**
     * Month 9
     */
    @Column(name = "october", nullable = false)
    private Integer october;
    
    /**
     * Month 10
     */
    @Column(name = "november", nullable = false)
    private Integer november;
    
    /**
     * Month 11
     */
    @Column(name = "december", nullable = false)
    private Integer december;

    public int getPercent(Month period) {
        return switch (period) {
            case JANUARY -> january;
            case FEBRUARY -> february;
            case MARCH -> march;
            case APRIL -> april;
            case MAY -> may;
            case JUNE -> june;
            case JULY -> july;
            case AUGUST -> august;
            case SEPTEMBER -> september;
            case OCTOBER -> october;
            case NOVEMBER -> november;
            case DECEMBER -> december;
        };
    }

    public void setPercent(Month period, int newPercent) {
        switch (period) {
            case JANUARY -> january = newPercent;
            case FEBRUARY -> february = newPercent;
            case MARCH -> march = newPercent;
            case APRIL -> april = newPercent;
            case MAY -> may = newPercent;
            case JUNE -> june = newPercent;
            case JULY -> july = newPercent;
            case AUGUST -> august = newPercent;
            case SEPTEMBER -> september = newPercent;
            case OCTOBER -> october = newPercent;
            case NOVEMBER -> november = newPercent;
            case DECEMBER -> december = newPercent;
        }
    }
}
