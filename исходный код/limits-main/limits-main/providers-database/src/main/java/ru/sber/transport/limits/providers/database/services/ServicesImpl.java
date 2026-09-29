package ru.sber.transport.limits.providers.database.services;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.tables.Services;
import ru.sber.transport.database.limits.tables.records.ServicesRecord;
import ru.sber.transport.limits.model.Service;

import java.util.List;
import java.util.Optional;

public class ServicesImpl implements ru.sber.transport.limits.providers.Services, JooqRepository<Services, ServicesRecord, String> {

    @Override
    public Services table() {
        return Tables.SERVICES;
    }

    @Override
    public Optional<Service> get(String service) {
        return findById(service).map(this::toBusiness);
    }

    @Override
    public List<Service> get() {
        return findAll().stream().map(this::toBusiness).toList();
    }

    private Service toBusiness(ServicesRecord servicesRecord) {
        return servicesRecord::getId;
    }
}
