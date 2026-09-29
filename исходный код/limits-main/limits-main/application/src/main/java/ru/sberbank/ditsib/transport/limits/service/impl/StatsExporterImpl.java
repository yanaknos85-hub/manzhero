package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;
import ru.sberbank.ditsib.transport.limits.service.StatsExportPerformer;
import ru.sberbank.ditsib.transport.limits.service.StatsExporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
class StatsExporterImpl implements StatsExporter {

    private final List<StatsExportPerformer> performers;

    @Override
    public Path export(List<LimitStatsDTO> limitStatsDTOs) throws IOException {
        var fileName = Path.of(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString());
        try (var workbook = new XSSFWorkbook();
             var fos = new FileOutputStream(fileName.toString())) {
            var maxLevel = getMaxLevel(limitStatsDTOs);
            var data = performers.stream()
                .sorted(Comparator.comparing(StatsExportPerformer::order))
                .map(p -> new AbstractMap.SimpleEntry<>(p, p.initSheet(workbook, maxLevel, limitStatsDTOs.getFirst())))
                .toList();

            for (var item : limitStatsDTOs) {
                data.forEach(e -> e.getKey().addItem(e.getValue(), maxLevel, item));
            }
            workbook.write(fos);
            data.forEach(d -> d.getKey().close(d.getValue()));
        }
        return fileName;
    }

    private int getMaxLevel(List<LimitStatsDTO> list) {
        int maxLevel = 0;
        for (var limitStatsDTO : list) {
            if (limitStatsDTO.getDepartmentLevel() > maxLevel) {
                maxLevel = limitStatsDTO.getDepartmentLevel();
            }
        }
        return maxLevel + 1;
    }

}
