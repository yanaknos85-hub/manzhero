package ru.sber.transport.tariff_fleet.service;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;

import java.util.List;
import java.util.UUID;

public interface ServicePointService {

    /**
     * Сохранение сервисных точек
     *
     * @param servicePoints сервисные точки
     */
    void saveAll(@NotEmpty List<ServicePoint> servicePoints);

    /**
     * Загрузка сервисных точек из файла
     *
     * @param documentType тип документа
     * @param file         файл с сервисными точками
     * @return результат загрузки
     */
    UploadServicePointsDto validateUploadServicePoints(DocumentType documentType, MultipartFile file);

    /**
     * Получить все сервисные точки по договору
     *
     * @param contractId ид договора
     * @return список сервисных точек
     */
    List<ServicePoint> findAllByContractId(UUID contractId);

    /**
     * Пересоздать сервисные точки для договора
     *
     * @param contractId    ид договора
     * @param servicePoints список сервисных точек
     * @return список сервисных точек
     */
    List<ServicePoint> dropExistedPointsForContractAndCreate(UUID contractId, List<ServicePointDto> servicePoints);

    /**
     * Скачать шаблон файла для загрузки сервисных точек
     *
     * @return файл шаблона
     */
    byte[] downloadServicePointsTemplate();

    /**
     * Формирование шаблона для загрузки сервисных точек
     *
     * @return файл с шаблоном
     */
    byte[] buildServicePointsFile(List<ServicePoint> servicePoints);

    /**
     * Деактивация сервисных точек по договору
     *
     * @param contractId идентификатор договора
     */
    void deactivateServicePointsForContract(UUID contractId);
}
