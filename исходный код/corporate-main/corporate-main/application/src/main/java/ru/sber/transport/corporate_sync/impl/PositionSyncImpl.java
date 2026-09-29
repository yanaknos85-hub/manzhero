package ru.sber.transport.corporate_sync.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.MessageMapper;
import ru.sber.transport.messages.easup.avro.PositionData;

@Getter
@Component
@RequiredArgsConstructor
public class PositionSyncImpl extends OrganizationStructureSync<Position, PositionFilter, PositionData> {

    private final Provider<Position, PositionFilter> provider;

    private final MessageMapper<Position, PositionData> mapper;

    private final Sender<Position> sender;

    private final AvailableClassesProvider availableClassesProvider;

    @Override
    protected String getId(PositionData data) {
        return data.getId();
    }

    @Override
    protected Class<Position> getDataClass() {
        return Position.class;
    }

    @Override
    protected Class<PositionFilter> getFilterClass() {
        return PositionFilter.class;
    }

    @Override
    protected void updateLinks(OrganizationsCache cache, Position target, PositionData source) {
        target.setAvailableClasses(availableClassesProvider.get(target.getId()));
    }
}
