package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.PositionController;
import ru.sberbank.ditsib.corpclient.dto.NewPositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.PositionSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.PositionService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of PositionController
 */
@RequiredArgsConstructor
@RestController
class PositionControllerImpl implements PositionController {
    private final PositionService positionService;
    private final OrganizationService organizationService;
    private final PositionSender sender;
    
    @CheckOrganizationAccess
    @Override
    public PositionDTO savePosition(
            @Organization UUID organizationId,
            NewPositionDTO newPosition
                                   ) {
        organizationService.validateOrganizationId(organizationId);
        newPosition.setOrganizationId(organizationId);
        return positionService.savePosition(newPosition);
    }
    
    @CheckOrganizationAccess
    @Override
    public void editPosition(
            @Organization UUID organizationId,
            UUID positionId,
            PositionDTO updated
                            ) {
        getPosition(organizationId, positionId);
        updated.setOrganizationId(organizationId);
        positionService.updatePosition(updated);
        sender.send(updated, false);
    }
    
    @CheckOrganizationAccess
    @Override
    public void deletePosition(
            @Organization UUID organizationId,
            UUID positionId
                              ) {
        PositionDTO deleted = getPosition(organizationId, positionId);
        positionService.deletePosition(positionId);
        sender.send(deleted, true);
        
    }
    
    @CheckOrganizationAccess
    @Override
    public void restorePosition(@Organization UUID organizationId, UUID positionId) {
        PositionDTO toRestore = getPosition(organizationId, positionId);
        positionService.restorePosition(positionId);
        sender.send(toRestore, false);
    }
    
    @CheckOrganizationAccess
    @Override
    public PositionDTO getPosition(
            @Organization UUID organizationId,
            UUID positionId
                                  ) {
        organizationService.validateOrganizationId(organizationId);
        return positionService.getPosition(positionId);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<PositionDTO> getPositions(
            @Organization UUID organizationId
                                                         ) {
        organizationService.validateOrganizationId(organizationId);
        return positionService.getPositions(organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public List<PositionDTO> searchPositions(@Organization UUID organizationId, PositionSearchDTO positionSearchDTO,
                                             JwtAuthenticationToken authentication) {
        positionSearchDTO.setOrganizationId(organizationId);
        return positionService.search(positionSearchDTO);
    }
}
