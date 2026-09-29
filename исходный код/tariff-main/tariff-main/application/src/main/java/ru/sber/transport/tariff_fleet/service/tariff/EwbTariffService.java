package ru.sber.transport.tariff_fleet.service.tariff;

import org.springframework.data.domain.Page;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffGetByIdDto;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPatchDto;
import ru.sber.transport.tariff_fleet.dto.ewb.*;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с тарифами ЭПЛ
 */
public interface EwbTariffService {
    
    /**
     * Создание нового тарифа
     *
     * @param postTariffDto {@link EwbTariffPostDto}
     * @param tariff {@link Tariff}
     */
    void createTariff(EwbTariffPostDto postTariffDto, Tariff tariff, InspectionType inspectionType);
    
    /**
     * Получение списка тарифов
     *
     * @param searchTariffDto {@link EwbSearchTariffDto}
     *
     * @return {@link Page<EwbTariffGetDto>}
     */
    Page<EwbTariffGetDto> search(EwbSearchTariffDto searchTariffDto);
    
    /**
     * Изменение тарифа
     *
     * @param tariffId id тарифа
     * @param abstractTariffPatchDto {@link AbstractTariffPatchDto}
     */
    void edit(UUID tariffId, EwbTariffPatchDto abstractTariffPatchDto);
    
    /**
     * Получение тарифа по id
     *
     * @param tariffId id тарифа
     *
     * @return {@link AbstractTariffGetByIdDto}
     */
    EwbTariffGetByIdDto get(UUID tariffId);
    
    /**
     * Получаем список идентификаторов подразделений по идентфикатору договора
     *
     * @param contractId Идентификатор записи о договоре
     *
     * @return Список идентификаторов подразделений
     */
    List<UUID> findActiveTariffDepartmentIds(UUID contractId);
    
    /**
     * Автоматическая активация тарифа по идентификатору договора
     *
     * @param contractId Идентификатор договора
     *
     * @return Список тарифов, которые были активированы {@link EwbTariff}
     */
    List<EwbTariff> autoActivateAllByContractId(UUID contractId);
    
    /**
     * Деактивация тарифа по идентификатору
     *
     * @param id Идентификатор записи о тарифе
     */
    void deactivate(UUID id);
    
    /**
     * Автоматическая деактивация тарифа по идентификатору договора
     *
     * @param contractId Идентификатор договора
     *
     * @return Список тарифов, которые были деактивированы {@link EwbTariff}
     */
    List<EwbTariff> autoDeactivateAllByContractId(UUID contractId);
}
