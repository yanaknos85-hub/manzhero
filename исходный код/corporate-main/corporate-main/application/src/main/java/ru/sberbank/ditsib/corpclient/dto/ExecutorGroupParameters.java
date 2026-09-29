package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;

import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.TransportServiceType.EMPLOYEE_TRANSPORTATION;

@Setter
@Schema(title = "Параметры запроса группы исполнителей",
        description = "Параметры запроса данных группы исполнителей с сортировкой по humanReadableId")
public class ExecutorGroupParameters extends PageSortFilterParameters<ExecutorGroupField> {

    protected ExecutorGroupParameters() {
        super(ExecutorGroupField.ID);
    }

    @Schema(title = "Наименование группы исполнителей",
            description = "Фильтр сотрудника по наименованию группы исполнителей")
    private String executorGroupName;

    @Schema(title = "Организация исполнителя", description = "Фильтр по организации исполнителя")
    private UUID executorOrganization;

    @Schema(title = "ФИО исполнителя", description = "Фильтр по ФИО исполнителя")
    private String executorFIO;

    @Schema(title = "Табельный номер исполнителя", description = "Фильтр по табельному номеру исполнителя")
    private String executorPersonnelNumber;

    @Schema(title = "Организация заказчика", description = "Фильтр по организации заказчика")
    private List<UUID> customerOrganizations;

    @Schema(title = "Подразделение заказчика", description = "Фильтр по подразделению заказчика")
    private List<UUID> customerDepartments;

    @Schema(title = "Территория заказчика", description = "Фильтр по территории заказчика")
    private List<UUID> customerGeoZones;

    @Schema(title = "Имя сервиса", description = "Фильтр по имени сервиса")
    private TransportServiceType serviceType = EMPLOYEE_TRANSPORTATION;
}
