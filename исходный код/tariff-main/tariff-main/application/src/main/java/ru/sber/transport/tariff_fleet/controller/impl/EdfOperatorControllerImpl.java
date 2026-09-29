package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff_fleet.controller.EdfOperatorController;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;
import ru.sber.transport.tariff_fleet.service.EdfOperatorService;

import java.util.Set;

@RequiredArgsConstructor
@RestController
public class EdfOperatorControllerImpl implements EdfOperatorController {
    
    private final EdfOperatorService edfOperatorService;
    
    @Override
    public Set<EdfOperatorDto> getAllActive() {
        return edfOperatorService.getAllActive();
    }
}
