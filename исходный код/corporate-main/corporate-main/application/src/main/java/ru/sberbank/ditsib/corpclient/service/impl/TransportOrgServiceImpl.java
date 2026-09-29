package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.TransportOrgRepository;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.corpclient.messaging.sender.OrganizationSender;
import ru.sberbank.ditsib.corpclient.service.TransportOrgService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransportOrgServiceImpl implements TransportOrgService {
    
    private final TransportOrgRepository transportOrgRepository;

    private final OrganizationSender organizationSender;

    private final OrganizationRepository organizationRepository;
    
    @Override
    public List<String> getByOrganizationId(UUID organizationId) {
       final var list = transportOrgRepository.getByOrganizationId(organizationId);
       return list.stream().map(TransportOrg::getTransportType).toList();
    }
    
    @Override
    public void delete(UUID organizationId, String transportType) {
        transportOrgRepository.getByOrganizationIdAndTransportType(organizationId, transportType)
                .ifPresent(transportOrgRepository::delete);

        organizationRepository.findById(organizationId)
                .ifPresent(organization -> {
                    organization.setAvailableClasses(transportOrgRepository.getByOrganizationId(organization.getId()).stream().map(TransportOrg::getTransportType).toList());
                    organizationSender.send(organization);
                });
    }
  
    
    @Override
    public void save(UUID organizationId, String transportType) {
        var transportOrg = transportOrgRepository.getByOrganizationIdAndTransportType
                (organizationId, transportType).orElse(null);
        if (transportOrg == null) {
            var transportOrg1 = new TransportOrg();
            transportOrg1.setOrganizationId(organizationId);
            transportOrg1.setTransportType(transportType);
            transportOrgRepository.save(transportOrg1);

            organizationRepository.findById(organizationId)
                    .ifPresent(organization -> {
                        organization.setAvailableClasses(transportOrgRepository.getByOrganizationId(organization.getId()).stream().map(TransportOrg::getTransportType).toList());
                        organizationSender.send(organization);
                    });
        }
    }

    @Override
    public Optional<TransportOrg> getByOrganizationIdAndTransportType(UUID organizationId, String transportType) {
        return transportOrgRepository.getByOrganizationIdAndTransportType(organizationId, transportType);
    }
}
