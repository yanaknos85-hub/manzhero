package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.fuel.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.FileDownloadException;
import ru.sber.transport.tariff_fleet.exception.FileUploadException;
import ru.sber.transport.tariff_fleet.mapper.FuelContractMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelContractSender;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;
import ru.sber.transport.tariff_fleet.service.*;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;
import ru.sber.transport.tariff_fleet.service.tariff.FuelTariffService;
import ru.sber.transport.tariff_fleet.service.validation.FuelContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasText;
import static ru.sber.transport.tariff_fleet.exception.FileUploadException.S3_STORAGE_FILE_UPLOAD_ERROR;

@RequiredArgsConstructor
@Service
@Slf4j
public class FuelContractServiceImpl implements FuelContractService {

    private final FileService fileService;
    private final ContractorService contractorService;
    private final FuelContractValidationService fuelContractValidationService;
    private final FuelContractRepository fuelContractRepository;
    private final FuelContractMapper fuelContractMapper;
    private final ServicePointService servicePointService;
    private final ServicePointValidationService servicePointValidationService;
    private final FuelTariffService fuelTariffService;
    private final FuelContractSender fuelContractSender;
    private final RepairAndFuelServicePointService repairAndFuelServicePointService;
    private final FuelGrpcService fuelGrpcService;
    private final Clock clock;

    @Override
    public FuelContract findWithContract(UUID contractId) {
        return fuelContractRepository.findWithContractByContractId(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    @Override
    @Transactional
    public void createAllOrganizations(FuelContractPostAllOrganizationsDto contractDto, Contract contract) {
        fuelContractValidationService.validateContractorIdAndNumberUnique(contractDto.getOrganizationId(),
                contractDto.getContractorId(),
                contractDto.getNumber(),
                contract.getId());
        servicePointValidationService.validateServicePoint(contractDto.getServicePoints());
        var contractor = contractorService.getById(contractDto.getContractorId());
        fuelContractValidationService.validateContractor(contractDto.getServicePoints(), contractor);

        var fuelContract = fuelContractMapper.fuelContractPostDtoToFuelContract(contractDto, contract);
        saveLogo(fuelContract, contractDto.getLogo());
        fuelContractRepository.save(fuelContract);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
    }

    @Override
    @Transactional
    public void createSelfOrganization(FuelContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId) {
        fuelContractValidationService.validateContractorIdAndNumberUnique(organizationId, contractDto.getContractorId(), contractDto.getNumber(), contract.getId());
        servicePointValidationService.validateServicePoint(contractDto.getServicePoints());
        var contractor = contractorService.getById(contractDto.getContractorId());
        fuelContractValidationService.validateContractor(contractDto.getServicePoints(), contractor);

        var fuelContract = fuelContractMapper.fuelContractPostSelfOrganizationDtoToFuelContract(contractDto, contract, organizationId);
        saveLogo(fuelContract, contractDto.getLogo());
        fuelContractRepository.save(fuelContract);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
    }


    @Override
    @Transactional(readOnly = true)
    public ContractWithServicePointsFileDto getContractWithServicePointsFile(UUID id) {
        return buildContractWithServicePointsFile(findWithContract(id));
    }

    @Override
    public Page<FuelContractGetAllOrganizationsDto> searchAllOrganizations(FuelSearchContractAllOrganizationsDto searchContractDto) {
        return fuelContractRepository.searchFuelContracts(searchContractDto.getNumber(),
                        searchContractDto.getContractorId(),
                        searchContractDto.getOrganizationId(),
                        searchContractDto.getStart(),
                        searchContractDto.getEnd(),
                        searchContractDto.getActive(),
                        searchContractDto.getPageRequest())
                .map(fuelContractMapper::getFuelContractProjectionToFuelContractGetAllOrganizationsDto);
    }

    @Override
    public Page<FuelContractGetSelfOrganizationDto> searchSelfOrganization(
            FuelSearchContractSelfOrganizationDto searchContractDto, UUID organizationId
    ) {
        return fuelContractRepository.searchFuelContracts(searchContractDto.getNumber(),
                        searchContractDto.getContractorId(),
                        organizationId,
                        searchContractDto.getStart(),
                        searchContractDto.getEnd(),
                        searchContractDto.getActive(),
                        searchContractDto.getPageRequest())
                .map(fuelContractMapper::getFuelContractProjectionToFuelContractGetSelfOrganizationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractWithServicePointsFileDto getContractSelfWithServicePointsFile(UUID id, UUID organizationId) {
        var fuelContract = findWithContract(id);
        fuelContractValidationService.validateOrganizationPermission(fuelContract, organizationId);
        return buildContractWithServicePointsFile(fuelContract);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, MultipartFile file) {
        var fuelContract = findWithContract(contractId);
        fuelContractValidationService.validateContractActive(fuelContract.getContract());
        var contractor = contractorService.getById(fuelContract.getContractorId());
        fuelContractValidationService.validateContractTypeForServicePointUpdating(contractor);
        return repairAndFuelServicePointService.validateUploadServicePoints(file);
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileSelfOrganization(UUID contractId, MultipartFile file, UUID organizationId) {
        var fuelContract = findWithContract(contractId);
        fuelContractValidationService.validateOrganizationPermission(fuelContract, organizationId);
        fuelContractValidationService.validateContractActive(fuelContract.getContract());
        var contractor = contractorService.getById(fuelContract.getContractorId());
        fuelContractValidationService.validateContractTypeForServicePointUpdating(contractor);
        return repairAndFuelServicePointService.validateUploadServicePoints(file);
    }

    @Override
    @Transactional
    public void updatePartiallySelfOrganization(UUID id, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel, UUID organizationId) {
        var fuelContract = findWithContract(id);
        fuelContractValidationService.validateOrganizationPermission(fuelContract, organizationId);
        var result = partiallyUpdate(fuelContract, partiallyUpdateFuelContractModel);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(result));
    }

    @Override
    @Transactional
    public void updatePartiallyAllOrganizations(UUID id, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel) {
        var fuelContract = findWithContract(id);
        var result = partiallyUpdate(fuelContract, partiallyUpdateFuelContractModel);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(result));
    }

    @Override
    @Transactional
    public void deactivateSelfOrganization(UUID id, UUID organizationId) {
        var fuelContract = findWithContract(id);
        fuelContractValidationService.validateOrganizationPermission(fuelContract, organizationId);
        deactivate(fuelContract);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
    }

    @Override
    @Transactional
    public void deactivateAllOrganizations(UUID id) {
        var fuelContract = findWithContract(id);
        deactivate(fuelContract);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
    }

    @Override
    @Transactional
    public void autoActivate(UUID contractId) {
        var fuelContract = findWithContract(contractId);
        fuelContract.getContract().setActive(true);
        fuelContractRepository.save(fuelContract);
        fuelTariffService.activateByContractId(contractId);
        fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
    }

    @Override
    @Transactional
    public void autoDeactivate(UUID contractId) {
        var fuelContract = findWithContract(contractId);
        if (fuelContract.getContract().isActive()) {
            fuelTariffService.deactivateByContractId(contractId);
            fuelContract.getContract().setActive(false);
            fuelContract.getContract().setEnd(LocalDate.now(clock));
            fuelContractRepository.save(fuelContract);
            fuelContractSender.send(fuelContractMapper.toFuelContractMessage(fuelContract));
        }
    }

    @Override
    public boolean haveActiveContractsByContractorId(UUID contractorId) {
        return fuelContractRepository.existsByContractorIdAndContract_ActiveTrue(contractorId);
    }

    private FuelContract partiallyUpdate(FuelContract fuelContract, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel) {
        fuelContractValidationService.validateUpdateContract(fuelContract, partiallyUpdateFuelContractModel);
        partiallyUpdateFuelContractModel.amountWithVatOpt().ifPresent(fuelContract::setAmountWithVat);
        partiallyUpdateFuelContractModel.amountWithoutVatOpt().ifPresent(fuelContract::setAmountWithoutVat);
        partiallyUpdateFuelContractModel.servicePointsNameOpt().ifPresent(fuelContract::setServicePointsName);
        partiallyUpdateFuelContractModel.servicePointsOpt().ifPresent(
                servicePoints -> {
                    var savedPoints = servicePointService.dropExistedPointsForContractAndCreate(fuelContract.getContractId(), servicePoints);
                    fuelContract.setServicePoints(savedPoints);
                }
        );
        if (partiallyUpdateFuelContractModel.logoOpt().isPresent()) {
            saveLogo(fuelContract, partiallyUpdateFuelContractModel.logoOpt().orElse(""));
        } else {
            fuelContract.setLogoS3Id(null);
        }
        return fuelContractRepository.save(fuelContract);
    }

    private ContractWithServicePointsFileDto buildContractWithServicePointsFile(FuelContract fuelContract) {
        var servicePointsFileBase64 = ofNullable(servicePointService.buildServicePointsFile(fuelContract.getServicePoints())).map(it -> Base64.getMimeEncoder().encodeToString(it)).orElse(null);
        var logoFileBase64 = ofNullable(getLogo(fuelContract.getLogoS3Id())).map(it -> Base64.getMimeEncoder().encodeToString(it)).orElse(null);
        var isEditable = !ContractorType.API.equals(fuelContract.getContractor().getContractorType());
        return fuelContractMapper.toContractWithServicePointsFileDto(fuelContract, servicePointsFileBase64, logoFileBase64, isEditable);
    }

    private void saveLogo(FuelContract fuelContract, String logo) {
        if (hasText(logo)) {
            var logoS3Id = UUID.randomUUID();
            fuelContract.setLogoS3Id(logoS3Id);
            this.uploadFile(logoS3Id, logo);
        }
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

    private void deactivate(FuelContract fuelContract) {
        fuelContractValidationService.validateDeactivateContract(fuelContract);
        if (fuelContract.getContractor().getContractorType() != ContractorType.API) {
            servicePointService.deactivateServicePointsForContract(fuelContract.getContractId());
        }
        fuelGrpcService.deactivateFuelCardByContractId(fuelContract.getContractId());
        fuelContract.getContract().setActive(false);
        fuelContract.getContract().setEnd(LocalDate.now());
        fuelContractRepository.save(fuelContract);
    }
}
