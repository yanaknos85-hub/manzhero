package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeFileDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.CargoTypeSender;
import ru.sberbank.ditsib.corpclient.service.CargoTypeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для распознавания и записи информации о типах грузов.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CargoTypeResolverImpl implements DataImporter<CargoTypeFileDto>, DataExporter<CargoTypeFileDto> {

    private final CargoTypeService cargoTypeService;

    private final CargoTypeMapper mapper;

    private final CargoTypeSender sender;

    private final OrganizationService organizationService;

    @Override
    public @NonNull List<CargoTypeFileDto> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        return cargoTypeService.getAllActive().stream().map(mapper::entityToFileDto).toList();
    }

    @Override
    public void importData(CargoTypeFileDto newData, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var oldData = cargoTypeService.getByName(newData.getName());
        if (oldData != null) {
            cargoTypeService.deActivation(oldData.getId());
        }

        var cargoType = mapper.fileDtoToEntity(newData);
        cargoType.setOrganization(
                Optional.ofNullable(newData.getOrganizationId())
                        .map(organizationService::get)
                        .orElse(null)
        );

        cargoType = cargoTypeService.save(cargoType);
        sender.send(cargoType);
    }
}
