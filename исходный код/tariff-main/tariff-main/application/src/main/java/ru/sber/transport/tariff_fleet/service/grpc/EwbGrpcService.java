package ru.sber.transport.tariff_fleet.service.grpc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для взаимодействия с telemechanic (ewb) по grpc
 */
public interface EwbGrpcService {

    /**
     * Ищем активные ЭПЛ по списку идентификаторов подразделений
     *
     * @param departmentIds  список идентификаторов подразделений
     * @param checkStartDate дата начала проверки (включительно)
     * @return true если есть хотя бы один активный ЭПЛ
     */
    boolean haveActiveEwb(List<UUID> departmentIds, LocalDate checkStartDate);
}
