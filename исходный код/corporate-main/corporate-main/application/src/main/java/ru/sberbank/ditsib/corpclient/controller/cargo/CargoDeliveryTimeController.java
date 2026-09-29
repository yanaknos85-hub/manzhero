package ru.sberbank.ditsib.corpclient.controller.cargo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoDeliveryTimeDto;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * Controller for working with cargo delivery times.
 */
@RequestMapping("/cargo/deliverytime")
@Tag(name = "Грузы. Справочник сроков доставки", description = "Набор операций для работы со справочников сроков " +
                                                               "доставки")
public interface CargoDeliveryTimeController {
    
    /**
     * Get all delivery times.
     *
     * @return list of delivery times.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение всех активных сроков доставки")
    Collection<CargoDeliveryTimeDto> getAll();
    
    /**
     * Edit delivery times.
     * @param newData new data of delivery times.
     *
     * @return type
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение сроков доставки")
    Collection<CargoDeliveryTimeDto> update(@Valid @RequestBody List<CargoDeliveryTimeDto> newData);
}
