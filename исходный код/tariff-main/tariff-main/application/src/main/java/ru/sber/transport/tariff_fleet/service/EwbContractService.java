package ru.sber.transport.tariff_fleet.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.EwbContract;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.ewb.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сервис для работы с договорами ЭПЛ
 */
public interface EwbContractService {
    
    /**
     * Создание нового договора
     *
     * @param contractPostDto {@link EwbContractPostDto}
     * @param contract {@link Contract}
     */
    void create(EwbContractPostDto contractPostDto, Contract contract);
    
    /**
     * Получение списка договоров
     *
     * @param ewbSearchContractDto {@link EwbSearchContractDto}
     * @param start Начало периода
     * @param end Окончание периода
     *
     * @return {@link Page<EwbContractGetDto>}
     */
    Page<EwbContractGetDto> search(EwbSearchContractDto ewbSearchContractDto, LocalDate start, LocalDate end);
    
    /**
     * Создание нового тарифа
     *
     * @param tariffPostDto {@link EwbTariffPostDto}
     * @param tariff {@link Tariff}
     */
    void createTariff(EwbTariffPostDto tariffPostDto, Tariff tariff);
    
    /**
     * Редактирование договора
     *
     * @param id Идентификатор записи о договоре
     * @param contractPatchDto {@link EwbContractPatchDto}
     */
    void edit(UUID id, EwbContractPatchDto contractPatchDto);
    
    /**
     * Получение договора по идентификатору
     *
     * @param id Идентификатор записи о договоре
     *
     * @return {@link EwbGetContractByIdDto}
     */
    EwbGetContractByIdDto get(UUID id);
    
    /**
     * Получение договора по идентификатору
     *
     * @param id Идентификатор записи о договоре
     *
     * @return договор {@link EwbContract}
     */
    EwbContract getEwbContract(UUID id);
    
    /**
     * Получение договора по идентификатору c родительскими данными
     *
     * @param id Идентификатор записи о договоре
     *
     * @return договор {@link EwbContract}
     */
    EwbContract getEwbContractWithParentContract(UUID id);
    
    /**
     * Автоматическая активация договора
     *
     * @param contract договор {@link Contract}
     */
    void autoActivate(Contract contract);
    
    /**
     * Деактивация договора по идентификатору
     *
     * @param id Идентификатор записи о договоре
     */
    void deactivate(UUID id);
    
    /**
     * Автоматическая деактивация договора
     *
     * @param contractId Идентификатор договора
     */
    void autoDeactivate(UUID contractId);
}
