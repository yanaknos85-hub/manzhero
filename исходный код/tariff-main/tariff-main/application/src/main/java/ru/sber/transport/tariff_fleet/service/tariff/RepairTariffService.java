package ru.sber.transport.tariff_fleet.service.tariff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;
import ru.sber.transport.tariff_fleet.dto.SearchTariffResponse;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.UUID;

public interface RepairTariffService {
    /**
     * Создание тарифа
     * @param request - запроc для создания тарифа
     * @param tariff - тариф
     * @param contract - контракт
     * @param organizationId - id организации
     */
    void createTariff(CreateRepairTariffDto request, Tariff tariff, Contract contract, UUID organizationId);

    /**
     * Получение тарифа своей организации
     * @param id - id тарифа
     * @param organizationId - id организации
     * @return - тариф
     */
    AbstractTariffResponse getTariff(UUID id, UUID organizationId);

    /**
     * Получение тарифа (без проверки организации)
     * @param id - id тарифа
     * @return - тариф
     */
    AbstractTariffResponse getTariff(UUID id);

    /**
     * Деактивация тарифа своей организации
     * @param id - id тарифа
     * @param organizationId - id организации
     */
    void deactivateTariff(UUID id, UUID organizationId);

    /**
     * Деактивация тарифа (без проверки организации)
     * @param id - id тарифа
     */
    void deactivateTariff(UUID id);

    /**
     * Поиск тарифов
     * @param tariffFilter - фильтр
     * @param page - настройки пагинации
     * @return - список тарифов
     */
    Page<SearchTariffResponse> searchTariff(TariffFilter tariffFilter, Pageable page);

    /**
     * Получаем организацию по договору
     * @param contractId идентификатор договора
     * @return {@link Organization}
     */
    Organization getOrganizationByContract(UUID contractId);
}
