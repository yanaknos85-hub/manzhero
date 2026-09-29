package ru.sber.transport.tariff_fleet.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сервис для работы с договорами по ремонту
 */
public interface RepairContractService {

    /**
     * Поиск договора на ремонт с родительским договором
     *
     * @param contractId идентификатор договора
     * @return договор на ремонт с родительским договором {@link RepairContract}
     */
    RepairContract findWithContract(UUID contractId);

    void createAllOrganizations(RepairContractPostAllOrganizationsDto contractDto, Contract contract);

    void createSelfOrganization(RepairContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId);

    Page<RepairContractGetDto> search(RepairSearchContractDto searchContractDto, LocalDate start, LocalDate end);

    Page<RepairContractGetAllOrganizationsDto> searchAllOrganizations(RepairSearchContractAllOrganizationsDto searchContractDto);

    Page<RepairContractGetSelfOrganizationDto> searchSelfOrganization(RepairSearchContractSelfOrganizationDto searchContractDto, UUID organizationId);

    ContractWithServicePointsFileDto getContractWithServicePointsFile(UUID id);

    ContractWithServicePointsFileDto getContractSelfWithServicePointsFile(UUID id, UUID organizationId);

    /**
     * Проверка файла с точками обслуживания (все организации)
     *
     * @param contractId идентификатор договора
     * @param file       файл с точками обслуживания
     * @return {@link UploadServicePointsDto}
     */
    UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, MultipartFile file);

    /**
     * Проверка файла с точками обслуживания (своя организация)
     *
     * @param contractId     идентификатор договора
     * @param file           файл с точками обслуживания
     * @param organizationId идентификатор организации
     * @return {@link UploadServicePointsDto}
     */
    UploadServicePointsDto validateServicePointsFileSelfOrganization(UUID contractId, MultipartFile file, UUID organizationId);

    /**
     * Частичное обновление договора по ремонту своей организации
     *
     * @param id                                 идентификатор договора
     * @param partiallyUpdateRepairContractModel данные договора для обновления
     * @param organizationId                     идентификатор организации
     */
    void updatePartiallySelfOrganization(UUID id, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel, UUID organizationId);

    /**
     * Частичное обновление договора по ремонту
     *
     * @param id                                 идентификатор договора
     * @param partiallyUpdateRepairContractModel данные договора для обновления
     */
    void updatePartiallyAllOrganizations(UUID id, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel);

    /**
     * Деактивация договора по ремонту своей организации
     *
     * @param id             идентификатор договора
     * @param organizationId идентификатор организации
     */
    void deactivateSelfOrganization(UUID id, UUID organizationId);

    /**
     * Деактивация договора по ремонту
     *
     * @param id идентификатор договора
     */
    void deactivateAllOrganizations(UUID id);

    /**
     * Автоматическая активация договора по ремонту
     *
     * @param contractId идентификатор договора
     */
    void autoActivate(UUID contractId);

    /**
     * Автоматическая деактивация договора по ремонту
     *
     * @param contractId идентификатор договора
     */
    void autoDeactivate(UUID contractId);

    /**
     * Проверка наличия активных договоров по ремонту с указанным контрагентом
     *
     * @param contractorId идентификатор контрагента
     * @return true - если есть активные договоры по ремонту с указанным контрагентом, false - если нет
     */
    boolean haveActiveContractsByContractorId(UUID contractorId);
}
