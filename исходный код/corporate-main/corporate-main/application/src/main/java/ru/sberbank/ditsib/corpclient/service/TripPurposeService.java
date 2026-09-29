package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.dto.purpose.NewTripPurposeDTO;
import ru.sberbank.ditsib.corpclient.dto.purpose.TripPurposeDTO;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with trip purposes.
 */
public interface TripPurposeService {
    
    /**
     * Get trip purposes.
     *
     * @param uuid ID of trip purpose.
     *
     * @return trip purpose dto.
     */
    TripPurposeDTO get(UUID uuid);
    
    /**
     * Получение цели по названию и организации.
     *
     * @param name название.
     * @param id организация.
     * @return цель.
     */
    Optional<TripPurpose> get(String name, UUID id);

    /**
     * Add trip purposes.
     *
     * @param purposeId ID of trip purpose.
     * @param source data of trip purpose.
     *
     * @return trip purpose dto.
     */
    TripPurposeDTO updatePurpose(UUID organizationId, UUID purposeId, @NotNull NewTripPurposeDTO source);

    /**
     * Get trip purposes.
     *
     * @param uuid ID of trip purpose.
     * @param organization organization of trip purpose.
     *
     * @return trip purpose dto.
     */
    TripPurposeDTO getTripPurpose(@NotNull UUID uuid, @NotNull UUID organization);
    
    /**
     * Get all status trip purposes of organization
     *
     * @return all trip purposes
     */
    List<TripPurposeDTO> getActivePurpose(UUID organizationId);

    /**
     * Get all trip purposes of organization
     *
     * @return all trip purposes
     */
    List<TripPurposeDTO> getAllPurposes(UUID organizationId);
    
    /**
     * Search trip purposes by purpose substring
     *
     * @param searchString search string
     *
     * @return list of qualified dtos
     */
    List<TripPurposeDTO> findAllByPurposeLike(String searchString, @NotNull UUID organization);

    /**
     * Get trip purposes by attributes and organization
     *
     * @return list of trip purposes.
     */
    List<TripPurposeDTO> findAllByAttributes(@NotNull UUID userId, @NotNull UUID organization);

    /**
     * Delete trip purposes.
     *
     * @param purposeId ID of trip purpose.
     * @param organization organization of trip purpose.
     */
    void deletePurpose(@NotNull UUID purposeId, @NotNull UUID organization);

    /**
     * Сохранение цели поездки.
     *
     * @param purpose цель поездки
     * @return сохраненная цель
     */
    TripPurpose save(TripPurpose purpose);
    
    /**
     * Получение всех целей.
     *
     * @return цели поездки.
     */
    List<TripPurpose> getAll();
}
