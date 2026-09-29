package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.BonusController;
import ru.sberbank.ditsib.transport.limits.dto.BonusDTO;
import ru.sberbank.ditsib.transport.limits.exceptions.BonusLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.EmployeeNotFoundException;
import ru.sberbank.ditsib.transport.limits.mapper.BonusMapper;
import ru.sberbank.ditsib.transport.limits.service.BonusService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
@Scope("request")
public class BonusControllerImpl implements BonusController {
    
    private final BonusService bonusService;
    private final EmployeeService employeeService;
    private final BonusMapper bonusMapper;
    
    @Override
    public BonusDTO get(UUID ownerId, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var employee = employeeService.getByUserId(userId);
        if (employee.isEmpty()) {
            throw new EmployeeNotFoundException(userId);
        }
        
        if (!(authentication.isAuthenticated() && employee.get().getId().equals(ownerId))) {
            throw new BonusLogicException(
                    "Attempting to access another user's balance. User id: %s, owner id %s"
                            .formatted(authentication.getName(), ownerId));
        }
        return bonusMapper.toBonusDTO(bonusService.get(ownerId));
    }
}
