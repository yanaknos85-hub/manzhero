package ru.sber.transport.tariff_fleet.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.fuel.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;

import java.util.UUID;

public interface FuelContractService {

    /**
     * Поиск топливного договора с родительским договором
     *
     * @param contractId идентификатор договора
     * @return топливный договор с родительским договором {@link FuelContract}
     */
    FuelContract findWithContract(UUID contractId);

    void createAllOrganizations(FuelContractPostAllOrganizationsDto contractDto, Contract contract);

    void createSelfOrganization(FuelContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId);

    Page<FuelContractGetAllOrganizationsDto> searchAllOrganizations(FuelSearchContractAllOrganizationsDto searchContractDto);

    Page<FuelContractGetSelfOrganizationDto> searchSelfOrganization(FuelSearchContractSelfOrganizationDto searchContractDto, UUID organizationId);

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
     * @param organizationId id организации
     * @return {@link UploadServicePointsDto}
     */
    UploadServicePointsDto validateServicePointsFileSelfOrganization(UUID contractId, MultipartFile file, UUID organizationId);

    /**
     * Частичное обновление топливного договора своей организации
     *
     * @param id                               идентификатор договора
     * @param partiallyUpdateFuelContractModel данные для обновления
     * @param organizationId                   идентификатор организации
     */
    void updatePartiallySelfOrganization(UUID id, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel, UUID organizationId);

    /**
     * Частичное обновление топливного договора
     *
     * @param id                               идентификатор договора
     * @param partiallyUpdateFuelContractModel данные для обновления
     */
    void updatePartiallyAllOrganizations(UUID id, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel);

    /**
     * Деактивация топливного договора своей организации
     *
     * @param id             идентификатор договора
     * @param organizationId идентификатор организации
     */
    void deactivateSelfOrganization(UUID id, UUID organizationId);

    /**
     * Деактивация топливного договора
     *
     * @param id идентификатор договора
     */
    void deactivateAllOrganizations(UUID id);

    /**
     * Автоматическая активация топливного договора
     *
     * @param contractId топливный договор
     */
    void autoActivate(UUID contractId);

    /**
     * Автоматическая деактивация топливного договора
     *
     * @param contractId топливный договор
     */
    void autoDeactivate(UUID contractId);

    /**
     * Проверка наличия активных топливных договоров с указанным контрагентом
     *
     * @param contractorId идентификатор контрагента
     * @return true - если есть активные топливные договоры с указанным контрагентом, false - если нет
     */
    boolean haveActiveContractsByContractorId(UUID contractorId);
}
