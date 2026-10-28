package ru.sber.transport.trip.registries.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.trip_registries.grpc.dto.RegistryDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка маппера данных")
class RegistryMapperTest {

    private final RegistryMapper registryMapper = Mappers.getMapper(RegistryMapper.class);

    public static final String REGISTRY_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";

    @Test
    @DisplayName("Проверка идентификатора в верхнем регистре")
    void test_toApi() {
        String result = registryMapper.toApi(RegistryDescriptor.Registry.newBuilder().setRegistryId(REGISTRY_ID).build()).getRegistryId();
        assertThat(result).isEqualTo(REGISTRY_ID.toUpperCase());
    }
}