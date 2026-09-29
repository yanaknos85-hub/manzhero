package ru.sber.transport.corporate.providers.position.active_classes;

import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.database.corporate.tables.PositionTaxiClasses;
import ru.sber.transport.database.corporate.tables.records.PositionTaxiClassesRecord;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class AvailableClassesProviderImpl implements AvailableClassesProvider,
    JooqRepository<PositionTaxiClasses, PositionTaxiClassesRecord, UUID> {

    @Override
    public Set<String> get(UUID positionId) {
        return context().select(table().TAXI_CLASS).from(table())
            .where(table().POSITION_ID.eq(positionId))
            .fetchInto(String.class)
            .stream().collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAll() {
        return context().selectDistinct(table().TAXI_CLASS).from(table())
            .fetchInto(String.class)
            .stream().collect(Collectors.toSet());
    }

    @Override
    public PositionTaxiClasses table() {
        return PositionTaxiClasses.POSITION_TAXI_CLASSES;
    }

}
