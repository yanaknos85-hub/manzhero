package ru.sber.transport.tariff_fleet.service;

import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with contractors
 */
public interface ContractorService {
    
    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor.
     *
     * @return Optional of contractor. {@link Optional<Contractor>}
     */
    Optional<Contractor> get(UUID id);
    
    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor.
     *
     * @return contractor. {@link Contractor}
     */
    Contractor getById(UUID id);
    
    /**
     * Delete contractor.
     *
     * @param entity contractor to delete.
     */
    void delete(Contractor entity);
    
    /**
     * Save or update contractor.
     *
     * @param entity contractor to save. {@link Contractor}
     */
    void save(Contractor entity);
    
    /**
     * Get all active contractors.
     *
     * @return list of active contractors with AUTOSERVICE service_type {@link ContractorDto}
     */
    List<ContractorDto> getAllActive();
    
    /**
     * Get all active contract contractors by service type and document type.
     * @param serviceType
     * @param documentType
     * @return {@link ContractorDto}
     */
    List<ContractorDto> findAllOrganizationsByActiveContractsAndServiceTypeAndDocumentType(ServiceType serviceType,
                                                                                               DocumentType documentType);
    
    /**
     * Get self active contract contractors by service type and document type.
     * @param serviceType
     * @param documentType
     * @param organizationId
     * @return {@link ContractorDto}
     */
    List<ContractorDto> findSelfOrganizationsByActiveContractsAndServiceTypeAndDocumentType(ServiceType serviceType,
                                                                                            DocumentType documentType, UUID organizationId);
}
