package ru.sberbank.ditsib.corpclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ДТО фильтров для выгрузки справочников в файл.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileExportFilterDTO {

    /**
     * Идентификатор организации.
     */
    private UUID organizationId;
}
