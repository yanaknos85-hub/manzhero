package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate.providers.position.PositionProvider;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.PositionSyncMessageMapperImpl;
import ru.sber.transport.messages.easup.avro.PositionData;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка синхронизации должностей")
class PositionSyncImplTest {

    private final PositionProvider provider = mock(PositionProvider.class);

    private final Sender<Position> sender = mock(Sender.class);

    private final AvailableClassesProvider availableClassesProvider = mock(AvailableClassesProvider.class);

    private final BaseSync<PositionData> sync = new PositionSyncImpl(provider, new PositionSyncMessageMapperImpl(new ActiveMessageMapperImpl()), sender, availableClassesProvider);

    @Test
    @DisplayName("Синхронизация")
    void test_sync() {
        var cache = mock(OrganizationsCache.class);
        var data = PositionData.newBuilder()
                .setName(Instancio.create(String.class))
                .setId(Instancio.create(String.class))
                .setCode(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .setUpdateDate(Instancio.create(Instant.class))
                .setStartDate(Instancio.create(LocalDate.class))
                .setActive(Instancio.create(Boolean.class))
                .setChief(Instancio.create(Boolean.class))
                .build();

        when(provider.save(any())).then(it -> it.getArgument(0));

        sync.doSync(cache, data);

        var departmentCaptor = ArgumentCaptor.forClass(Position.class);
        verify(provider).saveSync(departmentCaptor.capture());

        assertThat(departmentCaptor.getValue().getName()).isEqualTo(data.getName());
    }

}