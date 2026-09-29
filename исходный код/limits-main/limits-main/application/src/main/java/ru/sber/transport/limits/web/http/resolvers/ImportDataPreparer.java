package ru.sber.transport.limits.web.http.resolvers;

import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;

import java.util.UUID;

/**
 * Подготовитель данных для импорта.
 */
public interface ImportDataPreparer {

    /**
     * Подготовить данные.
     *
     * @param organizationId организация.
     * @param authorId автор.
     */
    void prepare(UUID organizationId, UUID authorId);

    /**
     * Очистить объект.
     *
     * @param organizationId организация.
     */
    void clear(UUID organizationId);

    /**
     * Добавить элемент в процедуру импорта.
     *
     * @param organizationId организация.
     * @param source исходные данные.
     */
    void add(UUID organizationId, LimitDataFileDto source);

    /**
     * Зафиксировать данные в базе.
     *
     * @param organizationId идентификатор организации.
     */
    void persist(UUID organizationId);

    /**
     * Получение года, для которого настроен импортер.
     *
     * @param organizationId организация.
     * @return год.
     */
    int getYear(UUID organizationId);
}
