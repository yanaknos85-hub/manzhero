package ru.sberbank.ditsib.corpclient.service.cargo;

import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Service for working with delivery times.
 */
public interface CargoDeliveryTimeService {

    /**
     * Get all delivery times
     *
     * @return all delivery times
     */
    List<CargoDeliveryTime> getAll();
    
    /**
     * Edit delivery times.
     *
     * @param source data of delivery times.
     *
     * @return map of deleted and saved items.
     */
    Map<String, List<Object>> update(@NotNull List<CargoDeliveryTime> source);

    /**
     * Get key of updated state.
     *
     * @return key of updated state.
     */
    default String updatedKey() {
        return "updated";
    }

    /**
     * Get key of saved state.
     *
     * @return key of saved state.
     */
    default String savedKey() {
        return "saved";
    }
}
