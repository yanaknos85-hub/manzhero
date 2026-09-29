package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Exporter of statistics.
 */
public interface StatsExporter {

    /**
     * Export data to file.
     *
     * @param limitStatsDTOs data.
     * @return file content.
     */
    Path export(List<LimitStatsDTO> limitStatsDTOs) throws IOException;

}
