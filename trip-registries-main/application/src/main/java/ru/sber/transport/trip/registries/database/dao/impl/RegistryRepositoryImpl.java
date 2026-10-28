package ru.sber.transport.trip.registries.database.dao.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.database.trip_registries.Tables;
import ru.sber.transport.database.trip_registries.tables.Registry;
import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;
import ru.sber.transport.trip.registries.database.dao.RegistryRepository;
import ru.sber.transport.trip.registries.database.model.RegistryState;

import java.util.Collection;
import java.util.List;

@Component
public class RegistryRepositoryImpl implements RegistryRepository {

    @Override
    public List<RegistryRecord> findByRegistryStateIn(Collection<RegistryState> registryStates) {
        return context()
                .selectFrom(table())
                .where(table().REGISTRY_STATE.in(registryStates))
                .fetchInto(RegistryRecord.class);
    }

    @Override
    public Registry table() {
        return Tables.REGISTRY;
    }
}
