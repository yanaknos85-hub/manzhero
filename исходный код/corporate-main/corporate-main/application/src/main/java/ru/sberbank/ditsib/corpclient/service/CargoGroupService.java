package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.dto.cargo.CagroGroupTypeDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoGroupDto;

import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with cargo types.
 */
public interface CargoGroupService {

    Collection<CargoGroup> getTypesByGroupName(String groupName);

    CargoGroup save(CargoGroup newData);

    void delete(UUID groupId);
}
