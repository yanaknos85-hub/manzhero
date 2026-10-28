package ru.sber.transport.trip.registries.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sber.transport.trip.registries.api.dto.RegistryItem;
import ru.sber.transport.trip.registries.api.dto.RegistryRequest;
import ru.sber.transport.trip_registries.grpc.dto.RegistryDescriptor;

@Mapper(componentModel = "spring")
public interface RegistryMapper {

    @Mapping(target = "registryId", qualifiedByName = "registryIdToUpperCase")
    RegistryRequest toApi(RegistryDescriptor.Registry request);

    RegistryItem toApi(RegistryDescriptor.RegistryItem request);

    @Named("registryIdToUpperCase")
    default String registryIdToUpperCase(String source) {
        return source.toUpperCase();
    }
}
