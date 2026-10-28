package ru.sber.transport.trip.registries.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import ru.sber.transport.trip.registries.api.dto.*;

import java.util.UUID;

/**
 * Сервис для связи с контрагентами.
 */
public interface ApiService {

    /**
     * Отсылка реестра.
     *
     * @return статус запроса.
     */
    HttpStatusCode sendRegistry(RegistryRequest registryRequest);

    /**
     * Получение номера акта.
     *
     * @param registryId registryId
     * @return ответ с номером.
     */
    ActNumberResponse getActNumber(UUID registryId);

    /**
     * Получение статуса акта.
     *
     * @param actId actId.
     * @return ответ со статусом
     */
    ActStatusResponse getActStatus(String actId);



}
