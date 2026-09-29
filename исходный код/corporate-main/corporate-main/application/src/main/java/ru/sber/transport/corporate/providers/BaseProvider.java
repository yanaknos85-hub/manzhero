package ru.sber.transport.corporate.providers;

import org.jooq.*;
import org.jooq.Record;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.providers.mappers.DatabaseMapper;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Базовый провайдер данных
 *
 * @param <T> тип данных
 */
public abstract class BaseProvider<T extends HasOrganizationStructure, F extends Filter, R extends Record> implements Provider<T, F>, JooqRepository<Table<R>, R, UUID> {

    @Override
    public Optional<T> get(UUID id) {
        return findById(id).map(getMapper()::toBusiness);
    }

    @Override
    public Stream<T> streamAll(Collection<UUID> ids) {
        return context().selectFrom(table()).where(id().in(ids)).fetchStream().map(getMapper()::toBusiness);
    }

    @Override
    public T save(T department) {
        var database = createItem();
        if (department.getId() != null) {
            database = findById(department.getId()).orElseGet(this::createItem);
        }
        getMapper().update(database, department);
        if (database.get(id()) == null) {
            database.set(id(), Optional.ofNullable(department.getId()).orElseGet(UUID::randomUUID));
        }
        if (database.get(humanReadableId()) == null) {
            database.set(humanReadableId(), getHumanReadableProvider().getNext(department.getOrganizationId()));
        }
        database.set(updateTime(), OffsetDateTime.now());
        database = save(database);
        return getMapper().toBusiness(database);
    }

    @Override
    public Collection<T> saveAll(Collection<T> source) {
        var organizations = new HashMap<UUID, Integer>();
        for (var position : source) {
            if (position.getHumanReadableId() == null) {
                var count = organizations.getOrDefault(position.getOrganizationId(), 0);
                organizations.put(position.getOrganizationId(), count + 1);
            }
        }
        var ids = new HashMap<UUID, List<String>>();
        var next = new HashMap<UUID, AtomicInteger>();
        for (var entry : organizations.entrySet()) {
            var id = getHumanReadableProvider().getNext(entry.getKey(), entry.getValue());
            ids.put(entry.getKey(), id);
            next.put(entry.getKey(), new AtomicInteger(0));
        }
        var data = source.stream().map(getMapper()::toDatabase).map(it -> {
            if (it.get(humanReadableId()) == null) {
                var organizationId = it.get(organizationId());
                var id = next.get(organizationId);
                it.set(humanReadableId(), ids.get(organizationId).get(id.getAndIncrement()));
                next.put(organizationId, id);
            }
            if (it.get(id()) == null) {
                it.set(id(), UUID.randomUUID());
            }
            it.set(updateTime(), OffsetDateTime.now());
            return it;
        }).toList();
        saveAll(data);
        return data.parallelStream().map(getMapper()::toBusiness).toList();
    }

    @Override
    public Optional<T> get(UUID organization, String syncId) {
        return context().selectFrom(table())
                .where(organizationId().eq(organization))
                .and(syncId().eq(syncId))
                .fetchOptional()
                .map(getMapper()::toBusiness);
    }

    @Override
    public void saveSync(T source) {
        if (source.getId() == null) {
            source.setId(UUID.randomUUID());
        }
        var database = getMapper().toDatabase(source);
        if (database.get(humanReadableId()) == null) {
            database.set(humanReadableId(), getHumanReadableProvider().getNext(source.getOrganizationId()));
            source.setHumanReadableId(database.get(humanReadableId()));
        }
        database.set(updateTime(), OffsetDateTime.now());
        var data = List.of(database);
        var fields = List.of(syncId(), organizationId());
        saveAll(data, ReflectionUtils.cast(fields));
    }

    @Override
    public Page<T> streamAll(F filter, Projection projection, Integer page, Integer size, String sort, Direction direction) {
        final var fields = switch (projection) {
            case Projection.FULL -> List.of(table().fields());
            case Projection.MIN, Projection.SELECT -> minProjection();
        };
        final var query = context().select(fields).from(table());
        final var filtered = filtering(query, table(), filter);
        final var total = context().fetchCount(filtered);
        final var pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort);
        final var wrapped = wrap(filtered, pageRequest);
        return new Page<>(new PageImpl<>(wrapped.fetchInto(table()).stream().map(getMapper()::toBusiness).toList(), pageRequest, total));
    }

    @Override
    public List<T> get() {
        return findAll().parallelStream().map(getMapper()::toBusiness).toList();
    }

    /**
     * Маппер базы данных.
     *
     * @return маппер.
     */
    protected abstract DatabaseMapper<T, R> getMapper();

    /**
     * Поле с человекочитаемым идентификатором
     *
     * @return поле с человекочитаемым идентификатором
     */
    protected abstract TableField<R, String> humanReadableId();

    /**
     * Поле с идентификатором организации
     *
     * @return поле с идентификатором организации
     */
    protected abstract TableField<R, UUID> organizationId();

    /**
     * Поле с идентификатором
     *
     * @return поле с идентификатором
     */
    protected abstract TableField<R, UUID> id();

    /**
     * Поле с временем обновления
     *
     * @return поле с временем обновления
     */
    protected abstract TableField<R, OffsetDateTime> updateTime();

    /**
     * Поле с идентификатором синхронизации
     *
     * @return поле с идентификатором синхронизации
     */
    protected abstract TableField<R, String> syncId();

    /**
     * Получение провайдера человекочитаемого идентификатора
     *
     * @return провайдер человекочитаемого идентификатора
     */
    protected abstract HumanReadableProvider<T> getHumanReadableProvider();

    /**
     * Создать новый элемент
     *
     * @return новый элемент
     */
    protected abstract R createItem();

    /**
     * Набор полей минимальной проекции.
     *
     * @return набор полей минимальной проекции
     */
    protected abstract List<TableField<R, ?>> minProjection();

    /**
     * Фильтрация выборки
     *
     * @param query исходная выборка
     * @param table данные таблицы
     * @param filter фильтр
     * @return отфильтрованная выборка
     */
    protected abstract SelectConditionStep<Record> filtering(SelectJoinStep<Record> query, Table<R> table, F filter);

}
