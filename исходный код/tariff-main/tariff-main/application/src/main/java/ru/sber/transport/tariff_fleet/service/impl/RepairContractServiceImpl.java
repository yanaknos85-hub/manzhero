package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.FileDownloadException;
import ru.sber.transport.tariff_fleet.exception.FileUploadException;
import ru.sber.transport.tariff_fleet.mapper.RepairContractMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairContractSender;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;
import ru.sber.transport.tariff_fleet.service.*;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;
import ru.sber.transport.tariff_fleet.service.validation.RepairContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasText;
import static ru.sber.transport.tariff_fleet.exception.FileUploadException.S3_STORAGE_FILE_UPLOAD_ERROR;

@RequiredArgsConstructor
@Service
@Slf4j
public class RepairContractServiceImpl implements RepairContractService {

    private final FileService fileService;
    private final ContractorService contractorService;
    private final RepairContractRepository repairContractRepository;
    private final RepairContractMapper repairContractMapper;
    private final RepairContractValidationService repairContractValidationService;
    private final ServicePointValidationService servicePointValidationService;
    private final ServicePointService servicePointService;
    private final RepairContractSender repairContractSender;
    private final RepairAndFuelServicePointService repairAndFuelServicePointService;
    private final TariffService tariffService;
    private final Clock clock;

    @Override
    public RepairContract findWithContract(UUID contractId) {
        return repairContractRepository.findWithContractByContractId(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    @Override
    @Transactional
    public void createAllOrganizations(RepairContractPostAllOrganizationsDto contractDto, Contract contract) {
        repairContractValidationService.validateContractorIdAndNumberUnique(contractDto.getOrganizationId(), contractDto.getContractorId(),
                contractDto.getNumber(), contract.getId());
        servicePointValidationService.validateServicePoint(contractDto.getServicePoints());
        var contractor = contractorService.getById(contractDto.getContractorId());
        repairContractValidationService.validateContractor(contractDto.getServicePoints(), contractor);

        var repairContract = repairContractMapper.repairContractPostDtoToRepairContract(contractDto, contract);
        saveLogo(repairContract, contractDto.getLogo());
        repairContractRepository.save(repairContract);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }

    @Override
    @Transactional
    public void createSelfOrganization(RepairContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId) {
        repairContractValidationService.validateContractorIdAndNumberUnique(organizationId, contractDto.getContractorId(), contractDto.getNumber(), contract.getId());
        servicePointValidationService.validateServicePoint(contractDto.getServicePoints());
        var contractor = contractorService.getById(contractDto.getContractorId());
        repairContractValidationService.validateContractor(contractDto.getServicePoints(), contractor);

        var repairContract = repairContractMapper.repairContractPostSelfOrganizationDtoToRepairContract(contractDto, contract, organizationId);
        repairContract.setContractor(contractor);
        saveLogo(repairContract, contractDto.getLogo());
        repairContractRepository.save(repairContract);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }

    @Override
    public Page<RepairContractGetDto> search(RepairSearchContractDto searchContractDto, LocalDate start, LocalDate end) {
        return repairContractRepository.searchRepairContracts(searchContractDto.getNumber(),
                        searchContractDto.getContractorId(),
                        null, start, end,
                        searchContractDto.getActive(),
                        searchContractDto.getPageRequest())
                .map(repairContractMapper::getRepairContractProjectionToRepairGetContractDto);
    }

    @Override
    public Page<RepairContractGetAllOrganizationsDto> searchAllOrganizations(RepairSearchContractAllOrganizationsDto searchContractDto) {
        return repairContractRepository.searchRepairContracts(searchContractDto.getNumber(),
                        searchContractDto.getContractorId(),
                        searchContractDto.getOrganizationId(),
                        searchContractDto.getStart(),
                        searchContractDto.getEnd(),
                        searchContractDto.getActive(),
                        searchContractDto.getPageRequest())
                .map(repairContractMapper::getRepairContractProjectionToRepairContractGetAllOrganizationsDto);
    }

    @Override
    public Page<RepairContractGetSelfOrganizationDto> searchSelfOrganization(
            RepairSearchContractSelfOrganizationDto searchContractDto, UUID organizationId
    ) {
        return repairContractRepository.searchRepairContracts(searchContractDto.getNumber(),
                        searchContractDto.getContractorId(),
                        organizationId,
                        searchContractDto.getStart(),
                        searchContractDto.getEnd(),
                        searchContractDto.getActive(),
                        searchContractDto.getPageRequest())
                .map(repairContractMapper::getRepairContractProjectionToRepairContractGetSelfOrganizationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractWithServicePointsFileDto getContractWithServicePointsFile(UUID id) {
        return buildContractWithServicePointsFile(findWithContract(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractWithServicePointsFileDto getContractSelfWithServicePointsFile(UUID id, UUID organizationId) {
        var repairContract = findWithContract(id);
        repairContractValidationService.validateOrganizationPermission(repairContract, organizationId);
        return buildContractWithServicePointsFile(repairContract);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, MultipartFile file) {
        var repairContract = findWithContract(contractId);
        repairContractValidationService.validateContractActive(repairContract.getContract());
        var contractor = contractorService.getById(repairContract.getContractorId());
        repairContractValidationService.validateContractTypeForServicePointUpdating(contractor);
        return repairAndFuelServicePointService.validateUploadServicePoints(file);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileSelfOrganization(UUID contractId, MultipartFile file, UUID organizationId) {
        var repairContract = findWithContract(contractId);
        repairContractValidationService.validateOrganizationPermission(repairContract, organizationId);
        repairContractValidationService.validateContractActive(repairContract.getContract());
        var contractor = contractorService.getById(repairContract.getContractorId());
        repairContractValidationService.validateContractTypeForServicePointUpdating(contractor);
        return repairAndFuelServicePointService.validateUploadServicePoints(file);
    }

    @Override
    @Transactional
    public void updatePartiallySelfOrganization(UUID id, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel, UUID organizationId) {
        var repairContract = findWithContract(id);
        repairContractValidationService.validateOrganizationPermission(repairContract, organizationId);
        repairContractValidationService.validateUpdateContract(repairContract, partiallyUpdateRepairContractModel);
        partiallyUpdate(repairContract, partiallyUpdateRepairContractModel);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }

    @Override
    @Transactional
    public void updatePartiallyAllOrganizations(UUID id, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel) {
        var repairContract = findWithContract(id);
        repairContractValidationService.validateUpdateContract(repairContract, partiallyUpdateRepairContractModel);
        partiallyUpdate(repairContract, partiallyUpdateRepairContractModel);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }

    @Override
    @Transactional
    public void deactivateSelfOrganization(UUID id, UUID organizationId) {
        var fuelContract = findWithContract(id);
        repairContractValidationService.validateOrganizationPermission(fuelContract, organizationId);
        deactivate(fuelContract);
    }

    @Override
    @Transactional
    public void deactivateAllOrganizations(UUID id) {
        var fuelContract = findWithContract(id);
        deactivate(fuelContract);
    }

    @Override
    @Transactional
    public void autoActivate(UUID contractId) {
        var repairContract = findWithContract(contractId);
        repairContract.getContract().setActive(true);
        repairContractRepository.save(repairContract);
        tariffService.activateByContractId(contractId);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }

    @Override
    @Transactional
    public void autoDeactivate(UUID contractId) {
        var repairContract = findWithContract(contractId);
        if (repairContract.getContract().isActive()) {
            tariffService.deactivateByContractId(contractId);
            repairContract.getContract().setActive(false);
            repairContract.getContract().setEnd(LocalDate.now(clock));
            repairContractRepository.save(repairContract);

            repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
        }
    }

    @Override
    public boolean haveActiveContractsByContractorId(UUID contractorId) {
        return repairContractRepository.existsByContractorIdAndContract_ActiveTrue(contractorId);
    }

    private void partiallyUpdate(RepairContract repairContract, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel) {
        partiallyUpdateRepairContractModel.amountWithVatOpt().ifPresent(repairContract::setAmountWithVat);
        partiallyUpdateRepairContractModel.amountWithoutVatOpt().ifPresent(repairContract::setAmountWithoutVat);
        partiallyUpdateRepairContractModel.servicePointsNameOpt().ifPresent(repairContract::setServicePointsName);
        partiallyUpdateRepairContractModel.servicePointsOpt().ifPresent(
                servicePoints -> {
                    var savedPoints = servicePointService.dropExistedPointsForContractAndCreate(repairContract.getContractId(), servicePoints);
                    repairContract.setServicePoints(savedPoints);
                }
        );
        if (partiallyUpdateRepairContractModel.logoOpt().isPresent()) {
            saveLogo(repairContract, partiallyUpdateRepairContractModel.logoOpt().orElse(""));
        } else {
            repairContract.setLogoS3Id(null);
        }
        repairContractRepository.save(repairContract);
    }

    private ContractWithServicePointsFileDto buildContractWithServicePointsFile(RepairContract repairContract) {
        var servicePointsFileBase64 = ofNullable(servicePointService.buildServicePointsFile(repairContract.getServicePoints())).map(it -> Base64.getMimeEncoder().encodeToString(it)).orElse(null);
        var logoFileBase64 = ofNullable(getLogo(repairContract.getLogoS3Id())).map(it -> Base64.getMimeEncoder().encodeToString(it)).orElse(null);
        var isEditable = !List.of(ContractorType.API, ContractorType.AUTOSERVICE_EXTERNAL).contains(repairContract.getContractor().getContractorType());
        return repairContractMapper.toContractWithServicePointsFileDto(repairContract, servicePointsFileBase64, logoFileBase64, isEditable);
    }

    private byte[] getLogo(UUID logoStorageId) {
        if (logoStorageId == null) {
            return new byte[0];
        }
        try {
            var fileData = fileService.get(logoStorageId.toString());
            return fileData.stream();
        } catch (IOException e) {
            log.error("Error while getting logo from S3", e);
            throw new FileDownloadException(logoStorageId.toString());
        }
    }

    private void saveLogo(RepairContract repairContract, String logo) {
        if (hasText(logo)) {
            var logoS3Id = UUID.randomUUID();
            repairContract.setLogoS3Id(logoS3Id);
            this.uploadFile(logoS3Id, logo);
        }
    }

    private void uploadFile(UUID logoId, String logo) {
        try {
            fileService.upload(new ByteArrayInputStream(Base64.getMimeDecoder().decode(logo)),
                    logoId.toString(),
                    ContentType.APPLICATION_OCTET_STREAM.getMimeType());
        } catch (IOException | IllegalArgumentException e) {
            log.error("Error while uploading logo to S3", e);
            throw new FileUploadException(S3_STORAGE_FILE_UPLOAD_ERROR);
        }
    }

    private void deactivate(RepairContract repairContract) {
        repairContractValidationService.validateDeactivateContract(repairContract);
        if (repairContract.getContractor().getContractorType() != ContractorType.API) {
            servicePointService.deactivateServicePointsForContract(repairContract.getContractId());
        }
        repairContractRepository.save(repairContract);
        repairContract.getContract().setActive(false);
        repairContract.getContract().setEnd(LocalDate.now());
        repairContractRepository.save(repairContract);

        repairContractSender.send(repairContractMapper.toRepairContractMessage(repairContract));
    }
}
