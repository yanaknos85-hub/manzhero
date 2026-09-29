package ru.sber.transport.limits.business;

import jakarta.validation.constraints.Min;
import lombok.NonNull;
import ru.sber.transport.dto.Page;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.LimitFilter;
import ru.sber.transport.limits.business.model.ModifiedLimit;
import ru.sberbank.ditsib.request.Direction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Интерфейс для работы с лимитами
 */
public interface Limits {

    /**
     * Удалить лимит
     *
     * @param limitId идентификатор лимита
     * @param userId  идентификатор пользователя
     * @param forceAllow флаг, указывающий на необходимость удаления лимита
     */
    void delete(UUID limitId, UUID userId, boolean forceAllow);

    /**
     * Получить лимит
     *
     * @param userId идентификатор пользователя
     * @param forceAllow флаг, указывающий на необходимость получения лимита
     * @param limitId идентификатор лимита
     * @return лимит
     */
    @NonNull Limit get(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId);

    /**
     * Получить лимит
     *
     * @param userId идентификатор пользователя
     * @param forceAllow флаг, указывающий на необходимость получения лимита
     * @param limitId идентификатор лимита
     * @param modifiedSince дата последнего изменения
     * @return лимит
     */
    @NonNull
    ModifiedLimit get(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince);

    /**
     * Получить хеш для лимита
     *
     * @param userId идентификатор пользователя
     * @param forceAllow флаг, указывающий на необходимость получения лимита
     * @param limitId идентификатор лимита
     * @param modifiedSince дата последнего изменения
     * @return хеш для лимита
     */
    @NonNull
    ModifiedLimit hash(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince);

    /**
     * Получить хеш для лимита
     *
     * @param userId идентификатор пользователя
     * @param forceAllow флаг, указывающий на необходимость получения лимита
     * @param limitId идентификатор лимита
     * @return хеш для лимита
     */
    @NonNull
    Limit hash(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId);

    /**
     * Обновить лимит
     *
     * @param limitId идентификатор лимита
     * @param userId идентификатор пользователя
     * @param forceAllow принудительное обновление
     * @param newData новые данные лимита
     * @param updatedFields измененные поля
     * @return обновленный лимит
     */
    Limit update(UUID limitId, UUID userId, boolean forceAllow, Limit newData, List<String> updatedFields);

    /**
     * Получить лимиты
     *
     * @param filter фильтр для получения лимитов
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с лимитами
     */
    Page<Limit> get(@NonNull LimitFilter filter, @NonNull @Min(0) Integer page, @NonNull @Min(1) Integer size, @NonNull String sort, @NonNull Direction direction);
}
