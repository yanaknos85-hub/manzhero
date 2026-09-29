package ru.sber.transport.tariff_fleet.service.tariff;

import org.springframework.data.domain.Page;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с тарифами
 */
public interface TariffService {
    
    /**
     * Создание нового тарифа
     *
     * @param postTariffDto {@link AbstractTariffPostDto}
     * @param humanReadableId Человекочитаемый идентификатор
     * @param active Флаг активности
     *
     * @return {@link Tariff}
     */
    Tariff create(AbstractTariffPostDto postTariffDto, String humanReadableId, boolean active);
    
    /**
     * Получение списка тарифов
     *
     * @param abstractSearchTariffDto {@link AbstractSearchTariffDto}
     *
     * @return {@link Page<AbstractTariffGetDto>}
     */
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractTariffGetDto> search(AbstractSearchTariffDto abstractSearchTariffDto);
    
    /**
     * Получение тарифа по идентификатору
     *
     * @param tariffId идентификатор тарифа
     *
     * @return {@link Tariff}
     */
    Tariff get(UUID tariffId);

    List<Tariff> getAllByContractId(UUID contractId);
    
    /**
     * Getting a tariff by identifier
     *
     * @param id tariff identifier
     *
     * @return {@link Tariff}
     */
    AbstractTariffGetByIdDto getById(UUID id);
    
    /**
     * Редактирование тарифа
     *
     * @param tariffId Идентификатор записи о тарифе
     * @param abstractTariffPatchDto {@link AbstractTariffPatchDto}
     */
    void edit(UUID tariffId, AbstractTariffPatchDto abstractTariffPatchDto);
    
    /**
     * Деактивация тарифа по идентификатору
     *
     * @param id Идентификатор записи о тарифе
     */
    void deactivate(UUID id);

    /**
     * Создание тарифа для всех организаций
     * @param request Запрос
     */
    void createTariffAllOrganizations(AbstractCreateTariffRequest request);

    /**
     * Создание тарифа для собственной организации
     * @param request Запрос
     * @param userId Идентификатор пользователя
     */
    void createTariffSelfOrganization(AbstractCreateTariffRequest request, UUID userId);

    /**
     * Получение тарифов для собственной организации
     * @param id Идентификатор записи о тарифе
     * @param userId Идентификатор пользователя
     * @return {@link AbstractTariffResponse}
     */
    AbstractTariffResponse getTariffSelfOrganization(UUID id, UUID userId);

    /**
     * Получение тарифов для всех организаций
     * @param id Идентификатор записи о тарифе
     * @return {@link AbstractTariffResponse}
     */
    AbstractTariffResponse getTariffAllOrganizations(UUID id);

    /**
     * Деактивация тарифа по идентификатору для всех организаций
     * @param id Идентификатор записи о тарифе
     * @param userId Идентификатор пользователя
     */
    void deactivateTariffSelfOrganization(UUID id, UUID userId);

    /**
     * Деактивация тарифа по идентификатору для всех организаций
     * @param id Идентификатор записи о тарифе
     */
    void deactivateTariffAllOrganizations(UUID id);

    /**
     * Поиск тарифов для всех организаций
     * @param request Запрос
     * @return {@link Page<SearchTariffResponse>}
     */
    Page<SearchTariffResponse> searchTariffAllOrganizations(SearchTariffRequest request);

    /**
     * Поиск тарифов для собственной организации
     * @param request Запрос
     * @param userId Идентификатор пользователя
     * @return {@link Page<SearchTariffResponse>}
     */
    Page<SearchTariffResponse> searchTariffSelfOrganization(SearchTariffRequest request, UUID userId);

    /**
     *  Активация тарифа по идентификатору
      * @param contractId Идентификатор договора
     */
    void activateByContractId(UUID contractId);

    /**
     * Деактивация тарифа по идентификатору
     * @param contractId Идентификатор договора
     */
    void deactivateByContractId(UUID contractId);
}
