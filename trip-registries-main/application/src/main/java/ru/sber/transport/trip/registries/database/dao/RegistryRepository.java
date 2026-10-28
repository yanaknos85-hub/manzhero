package ru.sber.transport.trip.registries.database.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.trip_registries.tables.Registry;
import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;
import ru.sber.transport.trip.registries.database.model.RegistryState;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с реестрами.
 */
public interface RegistryRepository extends JooqRepository<Registry, RegistryRecord, UUID> {

    /**
     * Получение реестров по состоянию.
     *
     * @param registryStates состояния.
     * @return список реестров.
     */
    List<RegistryRecord> findByRegistryStateIn(Collection<RegistryState> registryStates);
}
