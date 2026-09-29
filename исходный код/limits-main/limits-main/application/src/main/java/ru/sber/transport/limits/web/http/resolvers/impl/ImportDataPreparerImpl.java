package ru.sber.transport.limits.web.http.resolvers.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.limits.web.http.resolvers.ImportDataPreparer;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
class ImportDataPreparerImpl implements ImportDataPreparer {

    private final Map<UUID, OrganizationDataImporter> organizationData = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    @Override
    public void prepare(UUID organizationId, UUID authorId) {
        if (organizationData.containsKey(organizationId)) {
            throw new DuplicateDataException("Import limit", "organization_id", organizationId);
        }
        var cache = context.getBean(OrganizationDataImporter.class);
        cache.setAuthor(authorId);
        cache.setOrganizationId(organizationId);
        organizationData.put(organizationId, cache);
    }

    @Override
    public void clear(UUID organizationId) {
        organizationData.remove(organizationId);
    }

    @Override
    public void add(UUID organizationId, LimitDataFileDto source) {
        var cache = getCache(organizationId);
        if (cache.isEmpty() && "DEP".equals(source.getLimitType())) {
            var department = cache.getDepartment(source.getDepartmentCode());
            if (department == null) {
                throw new IllegalStateException("Подразделение %s не найдено".formatted(source.getDepartmentCode()));
            }
        }

        if (source.getBalance().compareTo(source.getSum()) > 0) {
            throw new IllegalStateException("Файл импорта содержит записи с балансом больше суммы");
        }

        var serviceType = cache.getServiceType();
        var currentServiceType = LimitServiceType.getLimitServiceTypeByTransportType(TransportTypeEnum.valueOf(source.getTransportType()));
        var year = cache.getYear();
        if (serviceType == null) {
            cache.setServiceType(currentServiceType.name());
        }
        if (year == null) {
            cache.setYear(source.getYear());
        }
        if (!Objects.equals(cache.getServiceType(), currentServiceType.name())) {
            throw new IllegalStateException("Файл импорта содержит разные типы услуг");
        }
        if (!Objects.equals(cache.getYear(), source.getYear())) {
            throw new IllegalStateException("Файл импорта содержит разные года");
        }
        if (!cache.add(source)) {
            throw new IllegalStateException("Файл импорта содержит дублирующиеся записи");
        }
    }

    @Override
    public void persist(UUID organizationId) {
        getCache(organizationId).persist();
    }

    @Override
    public int getYear(UUID organizationId) {
        return getCache(organizationId).getYear();
    }

    @NotNull
    private OrganizationDataImporter getCache(UUID organizationId) {
        var cache = organizationData.get(organizationId);
        if (cache == null) {
            throw new IllegalStateException("Preparer must be initialized before using. Please call prepare(UUID) to initialize");
        }
        return cache;
    }
}
