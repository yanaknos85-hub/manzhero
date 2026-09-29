package ru.sber.transport.tariff_fleet.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с договорами
 */
public interface ContractService {

    /**
     * Создание нового договора
     *
     * @param abstractContractPostDto {@link AbstractContractPostDto}
     */
    void create(AbstractContractPostDto abstractContractPostDto);

    /**
     * Создание договора (все организации)
     *
     * @param abstractContractPostAllOrganizationsDto {@link AbstractContractPostAllOrganizationsDto}
     */
    void createAllOrganizations(AbstractContractPostAllOrganizationsDto abstractContractPostAllOrganizationsDto);

    /**
     * Создание договора (своя организация)
     *
     * @param abstractContractPostSelfOrganizationDto {@link AbstractContractPostSelfOrganizationDto}
     * @param userId                                  Идентификатор пользователя
     */
    void createSelfOrganization(AbstractContractPostSelfOrganizationDto abstractContractPostSelfOrganizationDto, UUID userId);

    /**
     * Получение списка договоров
     *
     * @param abstractSearchContractDto {@link AbstractSearchContractDto}
     * @return {@link Page<AbstractContractGetDto>}
     */
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetDto> search(AbstractSearchContractDto abstractSearchContractDto);

    /**
     * Получение списка договоров (все организации)
     *
     * @param abstractSearchContractAllOrganizationsDto {@link AbstractSearchContractAllOrganizationsDto}
     * @return {@link Page<AbstractContractGetDto>}
     */
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetAllOrganizationsDto> searchAllOrganizations(
            AbstractSearchContractAllOrganizationsDto abstractSearchContractAllOrganizationsDto
    );

    /**
     * Получение списка договоров (своя организация)
     *
     * @param abstractSearchContractSelfOrganizationDto {@link AbstractSearchContractSelfOrganizationDto}
     * @return {@link Page<AbstractContractGetSelfOrganizationDto>}
     */
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetSelfOrganizationDto> searchSelfOrganization(
            AbstractSearchContractSelfOrganizationDto abstractSearchContractSelfOrganizationDto, UUID userId
    );

    /**
     * Редактирование договора
     *
     * @param contractId               Идентификатор записи о договоре
     * @param abstractContractPatchDto {@link AbstractContractPatchDto}
     */
    void edit(UUID contractId, AbstractContractPatchDto abstractContractPatchDto);

    /**
     * Получение договора по идентификатору
     *
     * @param id идентификатор договора
     * @return {@link AbstractGetContractByIdDto}
     */
    AbstractGetContractByIdDto get(UUID id);

    /**
     * Получение списка идентификаторов начавшихся договоров
     *
     * @return список идентификаторов
     */
    List<Contract> getAllStarted();

    /**
     * Получение списка идентификаторов завершенных договоров
     *
     * @return список идентификаторов
     */
    List<Contract> getAllEnded();

    /**
     * Создание нового тарифа
     *
     * @param abstractTariffPostDto {@link AbstractTariffPostDto}
     * @param humanReadableId       Человекочитаемый идентификатор
     */
    void createTariff(AbstractTariffPostDto abstractTariffPostDto, String humanReadableId);

    /**
     * Редактирование тарифа
     *
     * @param tariffId               Идентификатор записи о тарифе
     * @param abstractTariffPatchDto {@link AbstractTariffPatchDto}
     */
    void editTariff(UUID tariffId, AbstractTariffPatchDto abstractTariffPatchDto);

    /**
     * Деактивация договора по идентификатору
     *
     * @param id Идентификатор записи о договоре
     */
    void deactivate(UUID id);

    /**
     * Получение метаданных договора своей организации по идентификатору
     *
     * @param id Идентификатор записи о договоре
     */
    ContractWithServicePointsFileDto getContractSelfWithFuelStationPointsFile(UUID id, UUID userId);

    /**
     * Получение метаданных договора по идентификатору
     *
     * @param id Идентификатор записи о договоре
     */
    ContractWithServicePointsFileDto getContractAllWithFuelStationPointsFile(UUID id);

    /**
     * Валидация файла с точками обслуживания для редактирования (все организации)
     *
     * @param contractId   идентификатор договора
     * @param documentType тип документа
     * @param file         файл с точками обслуживания
     * @return {@link UploadServicePointsDto}
     */
    UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, DocumentType documentType, MultipartFile file);

    /**
     * Валидация файла с точками обслуживания для редактирования (своя организация)
     *
     * @param contractId   идентификатор договора
     * @param documentType тип документа
     * @param file         файл с точками обслуживания
     * @param userId       Идентификатор пользователя
     * @return {@link UploadServicePointsDto}
     */
    UploadServicePointsDto validateServicePointsFileSelfOrganization(
            UUID contractId, DocumentType documentType, MultipartFile file, UUID userId
    );

    /**
     * Частичное обновление договора своей организации
     *
     * @param contractId идентификатор договора
     * @param request    {@link AbstractContractUpdateRequest}
     * @param userId     идентификатор пользователя
     */
    void updatePartiallyContractSelfOrganization(UUID contractId, AbstractContractUpdateRequest request, UUID userId);

    /**
     * Частичное обновление договора
     *
     * @param contractId идентификатор договора
     * @param request    {@link AbstractContractUpdateRequest}
     */
    void updatePartiallyContractAllOrganizations(UUID contractId, AbstractContractUpdateRequest request);

    /**
     * Деактивация договора своей организации
     *
     * @param contractId идентификатор договора
     * @param userId     Идентификатор пользователя
     */
    void deactivateSelfOrganization(UUID contractId, UUID userId);

    /**
     * Деактивация договора организации
     *
     * @param contractId идентификатор договора
     */
    void deactivateAllOrganizations(UUID contractId);

    /**
     * Проверка наличия активных договоров по идентификатору контрагента
     *
     * @param contractorId идентификатор контрагента
     * @return true - есть активные договоры, false - нет
     */
    boolean haveActiveContractsByContractorId(UUID contractorId);

}
