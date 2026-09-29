package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.dto.NewPersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Personal cars crud and other operations
 */
public interface PersonalCarService {
    /**
     * @param source      data for creation or update
     * @param currentUser
     * @return createdEntity
     */
    PersonalCarDTO savePersonalCar(@Valid NewPersonalCarDTO source, UUID currentUser);
    
    /**
     * Get car by id and employee id Throws EntityNotFoundResponseException in case of no result
     *
     * @param id identifier of personalCar
     *
     * @return found personalCar
     */
    PersonalCarDTO getPersonalCarByIdAndEmployeeId(@NotNull UUID id, @NotNull UUID employeeId);
    
    /**
     * @param id identifier of personalCar to delete
     */
    void deletePersonalAuto(@NotNull UUID id);
    
    /**
     * @param source data for update
     * @param currentUser
     *
     * @return updated entity
     */
    PersonalCarDTO updatePersonalAuto(@NotNull PersonalCarDTO source, UUID currentUser);

    /**
     * Get users personal cars.
     *
     * @return collection of personal cars.
     */
    List<PersonalCarDTO> getPersonalCarsByUser(@NotNull UUID employeeId);
}
