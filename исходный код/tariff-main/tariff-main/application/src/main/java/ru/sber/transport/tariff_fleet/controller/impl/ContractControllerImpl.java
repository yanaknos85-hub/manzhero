package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.controller.ContractController;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.helper.UserAuthorizationHelper;
import ru.sber.transport.tariff_fleet.service.ContractService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class ContractControllerImpl implements ContractController {
    private final ContractService contractService;
    
    @Override
    public void create(AbstractContractPostDto abstractContractPostDto) {
        contractService.create(abstractContractPostDto);
    }
    
    @Override
    public void createAllOrganizations(AbstractContractPostAllOrganizationsDto abstractContractPostAllOrganizationsDto) {
        contractService.createAllOrganizations(abstractContractPostAllOrganizationsDto);
    }
    
    @Override
    public void createSelfOrganization(
            AbstractContractPostSelfOrganizationDto abstractContractPostSelfOrganizationDto,
            Authentication authentication
                                      ) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        contractService.createSelfOrganization(abstractContractPostSelfOrganizationDto, userId);
    }
    
    @Override
    public Page<? extends AbstractContractGetDto> search(AbstractSearchContractDto abstractSearchContractDto) {
        return contractService.search(abstractSearchContractDto);
    }
    
    @Override
    public Page<? extends AbstractContractGetAllOrganizationsDto> searchAllOrganizations(
            AbstractSearchContractAllOrganizationsDto abstractSearchContractAllOrganizationsDto
                                                                                        ) {
        return contractService.searchAllOrganizations(abstractSearchContractAllOrganizationsDto);
    }
    
    @Override
    public Page<? extends AbstractContractGetSelfOrganizationDto> searchSelfOrganization(
            AbstractSearchContractSelfOrganizationDto abstractSearchContractSelfOrganizationDto,
            Authentication authentication
                                                                                        ) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return contractService.searchSelfOrganization(abstractSearchContractSelfOrganizationDto, userId);
    }
    
    @Override
    public void edit(UUID contractId, AbstractContractPatchDto abstractContractPatchDto) {
        contractService.edit(contractId, abstractContractPatchDto);
    }
    
    @Override
    public AbstractGetContractByIdDto get(UUID contractId) {
        return contractService.get(contractId);
    }
    
    @Override
    public void deactivate(UUID contractId) {
        contractService.deactivate(contractId);
    }

    @Override
    public ContractWithServicePointsFileDto getContractSelfWithFuelStationPointsFile(UUID contractId, Authentication authentication) {
        return contractService.getContractSelfWithFuelStationPointsFile(contractId, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public ContractWithServicePointsFileDto getContractAllWithFuelStationPointsFile(UUID contractId) {
        return contractService.getContractAllWithFuelStationPointsFile(contractId);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, String documentType, MultipartFile file) {
        return contractService.validateServicePointsFileAllOrganizations(contractId, DocumentType.resolveDocumentType(documentType), file);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileSelfOrganization(
            UUID contractId, String documentType, MultipartFile file, Authentication authentication
                                                                           ) {
        return contractService.validateServicePointsFileSelfOrganization(contractId, DocumentType.resolveDocumentType(documentType), file,
                                                                         UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public void updatePartiallyContractSelfOrganization(UUID contractId, AbstractContractUpdateRequest request, Authentication authentication) {
        contractService.updatePartiallyContractSelfOrganization(contractId, request, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public void updatePartiallyContractAllOrganizations(UUID contractId, AbstractContractUpdateRequest request) {
        contractService.updatePartiallyContractAllOrganizations(contractId, request);
    }

    @Override
    public void deactivateContractSelfOrganization(UUID contractId, Authentication authentication) {
        contractService.deactivateSelfOrganization(contractId, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public void deactivateContractAllOrganizations(UUID contractId) {
        contractService.deactivateAllOrganizations(contractId);
    }

}
