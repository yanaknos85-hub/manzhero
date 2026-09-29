package ru.sber.transport.tariff_fleet.service.tariff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;
import ru.sber.transport.tariff_fleet.dto.SearchTariffResponse;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.UUID;

public interface FuelTariffService {
    /**
     * Метод получения тарифа по id
     * @param id - id тарифа
     * @param organizationId - id организации
     * @return - тариф
     */
    AbstractTariffResponse getTariff(UUID id, UUID organizationId);
    /**
     * Метод получения тарифа по id
     * @param id - id тарифа
     * @return - тариф
     */
    AbstractTariffResponse getTariff(UUID id);

    /**
     * Метод создания тарифа
     * @param request - запрос
     * @param tariff - тариф
     * @param contract - контракт
     * @param organizationId - id организации
     */
    void createTariff(CreateFuelTariffDto request, Tariff tariff, Contract contract, UUID organizationId);

    /**
     * Метод деактивации тарифа
     * @param id - id тарифа
     * @param organizationId - id организации
     */
    void deactivateTariff(UUID id, UUID organizationId);

    void deactivateTariff(UUID id);

    /**
     * Метод поиска тарифов
     * @param tariffFilter - фильтр
     * @param page - настройки пагинации
     * @return - список тарифов
     */
    Page<SearchTariffResponse> searchTariff(TariffFilter tariffFilter, Pageable page);

    /**
     * Авто активация тарифа
     * @param contractId идентификатор контракта
     */
    void activateByContractId(UUID contractId);

    /**
     * Авто деактивация тарифа
     * @param contractId идентификатор контракта
     */
    void deactivateByContractId(UUID contractId);

    /**
     * Получаем организацию по договору
     * @param contractId идентификатор договора
     * @return {@link Organization}
     */
    Organization getOrganizationByContract(UUID contractId);
}
