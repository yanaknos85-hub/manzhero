package ru.sber.transport.corporate_sync.impl;

import org.apache.avro.specific.SpecificRecord;
import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.corporate.business.model.StructureType;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.MessageMapper;

import java.util.UUID;

/**
 * Синхронизация оргструктуры
 *
 * @param <T> тип данных системы
 * @param <M> тип сообщения
 */
abstract class OrganizationStructureSync<T extends HasOrganizationStructure, F extends Filter, M extends SpecificRecord> extends BaseSync<M> {

    @Override
    protected void doSync(OrganizationsCache cache, M data) {
        var found = cache.get(getDataClass(), getFilterClass(), getId(data)).orElseGet(() -> createItem(cache.getOrganization(), data));
        getMapper().update(found, data);
        found.setOrganizationId(cache.getOrganization());
        found.setStructureType(StructureType.INTERNAL);
        updateLinks(cache, found, data);
        getProvider().saveSync(found);
        getSender().send(found);
        cache.put(getDataClass(), found.getSyncId(), found);
    }

    protected abstract Sender<T> getSender();

    protected void updateLinks(OrganizationsCache cache, T target, M source) {
    }

    private T createItem(UUID organization, M data) {
        var item = getMapper().toBusiness(data);
        item.setId(UUID.randomUUID());
        item.setOrganizationId(organization);
        item.setStructureType(StructureType.INTERNAL);
        return item;
    }

    /**
     * Получить идентификатор
     *
     * @param data данные
     * @return идентификатор
     */
    protected abstract String getId(M data);

    /**
     * Получить класс данных
     *
     * @return класс данных
     */
    protected abstract Class<T> getDataClass();

    protected abstract Class<F> getFilterClass();

    /**
     * Получить маппер
     *
     * @return маппер
     */
    protected abstract MessageMapper<T, M> getMapper();

    /**
     * Получить провайдер
     *
     * @return провайдер
     */
    protected abstract Provider<T, F> getProvider();

}
