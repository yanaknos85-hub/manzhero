package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;

/**
 * Service for enriching entity type - {@link PersonalCar}
 */
public interface PersonalCarEnrichmentService {

    /**
     * Enrichment of new personal car entity with data about personal accept
     * @param personalCar {@link PersonalCar}- entity with data about personal car
     */
    void addUserAcceptInfo(PersonalCar personalCar);

}
