package ru.sberbank.ditsib.transport.limits.model.limit;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Model of statisitcs.
 *
 * @param period period.
 * @param transportType type of transport.
 * @param year year.
 * @param organizationId ID of organization.
 * @param budget budget.
 * @param spending spent sum.
 * @param <P> type of period.
 */
public record LimitStats<P extends Period> (
    UUID organizationId,
    Integer year,
    P period,
    TransportTypeEnum transportType,
    BigDecimal budget,
    BigDecimal spending
){}
