package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.controller.ContractorController;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;
import ru.sber.transport.tariff_fleet.helper.UserAuthorizationHelper;
import ru.sber.transport.tariff_fleet.service.ContractorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContractorControllerImpl implements ContractorController {
    
    private final ContractorService contractorService;
    
    @Override
    public List<ContractorDto> getAllActive() {
        return contractorService.getAllActive();
    }
    
    @Override
    public List<ContractorDto> getAllOrganization(DocumentType documentType)  {
        return contractorService.findAllOrganizationsByActiveContractsAndServiceTypeAndDocumentType(ServiceType.AUTOSERVICE, documentType);
    }
    
    @Override
    public List<ContractorDto> getSelfOrganization(DocumentType documentType, Authentication authentication) {
        return contractorService.findSelfOrganizationsByActiveContractsAndServiceTypeAndDocumentType(ServiceType.AUTOSERVICE, documentType,
                                                                                                     UserAuthorizationHelper.getUserId(authentication));
    }
    
}
