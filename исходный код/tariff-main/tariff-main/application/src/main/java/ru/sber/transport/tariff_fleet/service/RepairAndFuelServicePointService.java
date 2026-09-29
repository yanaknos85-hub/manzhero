package ru.sber.transport.tariff_fleet.service;

import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;

public interface RepairAndFuelServicePointService {
    
    /**
     * Загрузка сервисных точек из файла
     *
     * @param file файл с сервисными точками
     *
     * @return результат загрузки
     */
    UploadServicePointsDto validateUploadServicePoints(MultipartFile file);
    
    /**
     * Формирование шаблона для загрузки сервисных точек
     *
     * @return файл с шаблоном
     */
    byte[] downloadServicePointsTemplate();

}
