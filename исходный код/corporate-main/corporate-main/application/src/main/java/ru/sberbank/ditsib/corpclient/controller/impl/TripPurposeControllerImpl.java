package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.TripPurposeController;
import ru.sberbank.ditsib.corpclient.dto.purpose.NewTripPurposeDTO;
import ru.sberbank.ditsib.corpclient.dto.purpose.TripPurposeDTO;
import ru.sberbank.ditsib.corpclient.service.TripPurposeService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
class TripPurposeControllerImpl implements TripPurposeController {
    
    private final TripPurposeService service;
    
    @CheckOrganizationAccess
    @Override
    public TripPurposeDTO getPurpose(UUID uuid, @Organization UUID organizationId) {
        return service.getTripPurpose(uuid, organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public void editPurpose(@Organization UUID organizationId, UUID purposeId, NewTripPurposeDTO newData) {
        service.getTripPurpose(purposeId, organizationId);
        service.updatePurpose(organizationId, purposeId, newData);
    }
    
    @CheckOrganizationAccess
    @Override
    public TripPurposeDTO savePurpose(@Organization UUID organizationId, NewTripPurposeDTO newData) {
        return service.updatePurpose(organizationId, null, newData);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<TripPurposeDTO> getPurposes(@Organization UUID organizationId) {
        return service.getActivePurpose(organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<TripPurposeDTO> getTripPurposesBySearchString(String value,
                                                                    @Organization UUID organization) {
        return service.findAllByPurposeLike(value, organization);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<TripPurposeDTO> getAllPurposes(@Organization UUID organizationId) {
        return service.getAllPurposes(organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<TripPurposeDTO> getTripPurposesByEmployee(
            JwtAuthenticationToken authentication,
            @Organization @PathVariable("organizationId") UUID organizationId) {
        return service.findAllByAttributes(UUID.fromString(authentication.getToken().getId()), organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public void deletePurpose(@Organization @PathVariable("organizationId") UUID organizationId,
                                   @PathVariable("id") UUID purposeId){
        service.deletePurpose(purposeId, organizationId);
    }
}
