package ru.sber.transport.corporate_sync.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.corporate_sync.Sync;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс синхронизации
 *
 * @param <M> тип данных
 */
@Slf4j
@RequiredArgsConstructor
abstract class BaseSync<M extends SpecificRecord> implements Sync<M> {

    private static final Map<String, OrganizationsCache> organizationsCache = new HashMap<>();

    @Override
    public void sync(String organizationId, String id, M data) {
        var cache = organizationsCache.computeIfAbsent(organizationId, it -> {
            var newCache = createCache();
            newCache.organization(it);
            return newCache;
        });
        doSync(cache, data);
        if (cache.isEmpty()) {
            organizationsCache.remove(organizationId);
        }
        log.info(synchronizindLog(organizationId, id, data));
    }

    protected String synchronizindLog(String organizationId, String id, M data) {
        var number = number(data);
        var count = count(data);
        var percent = ((double) number / (double) count) * 100D;
        return "Synchronized %s of organization %s %s %d/%d (%03f%%)".formatted(data.getClass().getSimpleName(), organizationId, id, number, count, percent);
    }

    @Lookup
    OrganizationsCache createCache() {
        return null;
    }

    private int number(M data) {
        return (Integer) data.get(data.getSchema().getField("number").pos());
    }

    private int count(M data) {
        return (Integer) data.get(data.getSchema().getField("count").pos());
    }

    /**
     * Синхронизация данных
     *
     * @param organizationsCache кэш организаций
     * @param data               данные
     */
    protected abstract void doSync(OrganizationsCache organizationsCache, M data);

}
