package ru.sber.transport.limits.business.providers;

import lombok.NonNull;
import ru.sber.transport.dto.Page;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.LimitFilter;
import ru.sber.transport.limits.business.model.ModifiedLimit;
import ru.sberbank.ditsib.request.Direction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер лимитов
 */
public interface LimitsProvider {

    /**
     * Получить данные верхнеуровневого лимита по идентификатору одного из детей
     *
     * @param id идентификатор лимита
     * @return лимит
     */
    Optional<Limit> upper(UUID id);

    /**
     * Получить данные лимита по идентификатору
     *
     * @param id идентификатор лимита
     * @return лимит
     */
    Optional<Limit> get(UUID id);

    /**
     * Сохранить лимит
     *
     * @param source лимит
     */
    void save(Limit source);

    /**
     * Получить дочерние лимиты
     *
     * @param limitId идентификатор лимита
     * @return дочерние лимиты
     */
    List<Limit> getChildren(UUID limitId);

    /**
     * Получить лимит
     *
     * @param limitId идентификатор лимита
     * @param modifiedSince дата последнего изменения
     * @return лимит
     */
    Optional<ModifiedLimit> get(@NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince);

    /**
     * Получить минимальную информацию по лимиту.
     * @param limitId идентификатор лимита
     * @return лимит
     */
    Optional<Limit> hash(@NonNull UUID limitId);

    /**
     * Получить минимальную информацию по лимиту.
     * @param limitId идентификатор лимита
     * @param modifiedSince дата последнего изменения
     * @return лимит
     */
    Optional<ModifiedLimit> hash(@NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince);

    /**
     * Обновить данные лимита.
     *
     * @param limit лимит.
     * @param newData новые данные лимита.
     * @param updatedFields обновленные поля.
     * @return сохраненные данные лимита
     */
    @NonNull Limit update(@NonNull Limit limit, @NonNull Limit newData, @NonNull List<String> updatedFields);

    /**
     * Получить список лимитов по организациям.
     *
     * @param searchOrganizationId идентификатор организации, в рамках которой осуществляется поиск
     * @param filter фильтр для получения лимитов
     * @param page номер страницы
     * @param size количество элементов на странице
     * @param sort поля сортировки
     * @param direction направление сортировки
     * @return страница с результатами поиска
     */
    @NonNull Page<Limit> get(UUID searchOrganizationId, @NonNull LimitFilter filter, @NonNull Integer page, @NonNull Integer size, @NonNull String sort, @NonNull Direction direction);

    /**
     * Получить данные лимита по распределению.
     *
     * @param limitSharingId идентификатор распределения лимита
     * @return лимит
     */
    Optional<Limit> getOfSharing(UUID limitSharingId);
}
