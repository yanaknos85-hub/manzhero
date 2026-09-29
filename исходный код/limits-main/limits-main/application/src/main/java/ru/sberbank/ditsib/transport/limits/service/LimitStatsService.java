package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitStats;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface LimitStatsService {
    
    /**
     * Get limit stats.
     *
     * @param limitLevelDTO limit.
     *
     * @return limit spending object.
     */
    List<LimitStatsDTO> getLimitStats(UUID organizationId, List<LimitLevelDTO> limitLevelDTO, LocalDate untilDate);

    List<LimitStatsV2DTO> getLimitStatsV2(UUID organizationId, List<LimitLevelDTO> limitLevelDTO, LocalDate untilDate);
    
    /**
     * Export results to xls.
     *
     * @param limitStatsDTOs limitStatsDTOs.
     * @return limit spending object.
     */
    Path exportToXlsx(List<LimitStatsDTO> limitStatsDTOs) throws
            IOException;

    /**
     * Export results to xls.
     *
     * @param limitStatsDTOs limitStatsDTOs.
     * @return limit spending object.
     */
    Path exportToXlsxV2(List<LimitStatsV2DTO> limitStatsDTOs) throws
            IOException;
    
    /**
     * Get limits list.
     *
     * @param year year
     * @param departmentId departmentId
     *
     * @return limit spending object.
     */
    List<LimitLevelDTO> getLimitsList(UUID organizationId, Integer year, UUID departmentId);

    <P extends Period> List<LimitStats<P>> getByOrganizationIdAndYearAndPeriodAndTransportType(
        Collection<UUID> organizationId, int year, Collection<P> period,
        Collection<TransportTypeEnum> transportType);

}
