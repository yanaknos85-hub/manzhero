package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.ActivePersonalCar;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;

import java.util.UUID;

/**
 * Service for working with personal transport.
 */
public interface ActivePersonalCarService {

    /**
     * Save new data.
     *
     * @param personalCarId ID of transport.
     * @param employeeId ID of employee.
     * @return transport data.
     */
    ActivePersonalCar saveOrUpdate(UUID personalCarId, UUID employeeId);

    /**
     * Get status personal transport.
     *
     * @param employeeId ID of employee.
     * @return personal transport.
     */
    PersonalCar getActive(UUID employeeId);
}
