package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.dto.purpose.PurposeFileDto;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.TripPurposeService;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Распознавание целей поездки.
 */
@RequiredArgsConstructor
@Transactional
@Component
class TripPurposeDataResolverImpl implements DataExporter<PurposeFileDto>, DataImporter<PurposeFileDto> {
    
    private final OrganizationService organizationService;
    
    private final TripPurposeService tripPurposeService;
    
    private final PurposeSender purposeSender;
    
    @Override
    public void importData(PurposeFileDto item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
            var organization = organizationService.get(item.getOrganization())
                                                  .orElseThrow();
            
            var purpose = tripPurposeService.get(item.getName(), organization.getId())
                    .orElseGet(() -> TripPurpose.builder().organization(organization).build());
            
            purpose.setLabel(item.getName());
            purpose.setActive(item.isActive());
            purpose.setPurposeType(TripPurposeType.valueOfRus(item.getType()));
            
            purpose = tripPurposeService.save(purpose);
            
            Optional.of(purpose).filter(pur -> pur.getId() != null).ifPresent(purposeSender::send);
    }
    
    @Override
    public @NonNull List<PurposeFileDto> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var result = new ArrayList<PurposeFileDto>();
        
        for (var purpose : tripPurposeService.getAll()) {
            var item = new PurposeFileDto();
            
            item.setName(purpose.getLabel());
            item.setOrganization(purpose.getOrganization().getOfficialName());
            item.setActive(purpose.isActive());
            item.setType(purpose.getPurposeType().getRusName());
            
            result.add(item);
        }
        
        return result;
    }
}
