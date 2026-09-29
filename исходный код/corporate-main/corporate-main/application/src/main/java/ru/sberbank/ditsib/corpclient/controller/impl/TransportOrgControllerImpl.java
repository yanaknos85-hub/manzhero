package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.TransportOrgController;
import ru.sberbank.ditsib.corpclient.dto.TransportOrgDto;
import ru.sberbank.ditsib.corpclient.dto.constant.TransportTypeEnumDTO;
import ru.sberbank.ditsib.corpclient.mapper.TransportTypeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.TransportOrgSender;
import ru.sberbank.ditsib.corpclient.service.TransportOrgService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация TransportOrgController
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TransportOrgControllerImpl implements TransportOrgController {

    private final TransportOrgService transportOrgService;

    private final TransportTypeMapper mapper;

    private final TransportOrgSender sender;

    @SuppressWarnings("java:S3958")
    @CheckOrganizationAccess
    @Override
    public List<TransportTypeEnumDTO> get(@Organization UUID organizationId) {
        return transportOrgService.getByOrganizationId(organizationId).stream()
                .map(elt -> TransportTypeEnumDTO.builder()
                        .name(elt)
                        .rusName(mapper.getRusName(elt))
                        .build())
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TransportTypeEnumDTO> getByServiceType(String transportServiceTypeId,
                                                       UUID organizationId) {
        return transportOrgService.getByOrganizationId(organizationId).stream()
                .filter(tt -> mapper.getServiceType(tt).equals(transportServiceTypeId))
                .map(elt -> TransportTypeEnumDTO.builder()
                        .name(elt)
                        .rusName(mapper.getRusName(elt))
                        .build())
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TransportOrgDto> getAllByServiceType(String transportServiceTypeId,
                                                     UUID organizationId) {
        final var list = transportOrgService.getByOrganizationId(organizationId);
        return list.stream()
                .filter(tt -> mapper.getServiceType(tt).equals(transportServiceTypeId))
                .map(elt -> new TransportOrgDto(elt, list.contains(elt)))
                .toList();
    }

    @CheckOrganizationAccess
    @Override
    public List<TransportOrgDto> getAll(@Organization UUID organizationId) {
        final var result = new ArrayList<TransportOrgDto>();
        final var list = transportOrgService.getByOrganizationId(organizationId);
        for (final var item : mapper.getTypes()) {
            var active = list.contains(item);
            result.add(new TransportOrgDto(item, active));
        }
        return result;
    }

    @CheckOrganizationAccess
    @Override
    public List<String> add(@Organization UUID organizationId, String transportType) {
        transportOrgService.save(organizationId, transportType);
        transportOrgService.getByOrganizationIdAndTransportType(organizationId, transportType)
                .ifPresent(t -> sender.send(t, false));
        return transportOrgService.getByOrganizationId(organizationId);
    }

    @CheckOrganizationAccess
    @Override
    public void addBatch(@Organization UUID organizationId, List<TransportOrgDto> list) {
        for (TransportOrgDto dto : list) {
            if (dto.isActive()) {
                transportOrgService.save(organizationId, dto.getTransportType());
                transportOrgService.getByOrganizationIdAndTransportType(organizationId, dto.getTransportType())
                        .ifPresent(t -> sender.send(t, false));
            } else {
                transportOrgService.getByOrganizationIdAndTransportType(organizationId, dto.getTransportType())
                        .ifPresent(t -> sender.send(t, true));
                transportOrgService.delete(organizationId, dto.getTransportType());
            }
        }
    }

    @CheckOrganizationAccess
    @Override
    public List<String> delete(@Organization UUID organizationId, String transportType) {
        transportOrgService.delete(organizationId, transportType);
        transportOrgService.getByOrganizationIdAndTransportType(organizationId, transportType)
                .ifPresent(t -> sender.send(t, true));
        return transportOrgService.getByOrganizationId(organizationId);
    }


}
