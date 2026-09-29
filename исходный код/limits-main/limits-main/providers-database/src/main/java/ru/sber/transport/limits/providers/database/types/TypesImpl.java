package ru.sber.transport.limits.providers.database.types;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.tables.Types;
import ru.sber.transport.database.limits.tables.records.TypesRecord;
import ru.sber.transport.limits.model.Type;

import java.util.Optional;

public class TypesImpl implements ru.sber.transport.limits.providers.Types, JooqRepository<Types, TypesRecord, String> {

    @Override
    public Optional<Type> get(String type) {
        return findById(type).map(this::createType);
    }

    @Override
    public Types table() {
        return Tables.TYPES;
    }

    private Type createType(TypesRecord typesRecord) {
        return typesRecord::getId;
    }
}
