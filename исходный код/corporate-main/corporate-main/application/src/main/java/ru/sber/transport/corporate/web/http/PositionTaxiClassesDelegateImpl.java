package ru.sber.transport.corporate.web.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.web.api.PositionsApi;

@Component
@RequiredArgsConstructor
public class PositionTaxiClassesDelegateImpl implements PositionsApi {

    private final EmployeeProvider employeeProvider;

    private final AvailableClassesProvider availableClassesProvider;

    @Override
    public ResponseEntity<List<String>> taxiClassesGet(UUID userId, String personalNumber) {
        if (Objects.isNull(userId) && StringUtils.isBlank(personalNumber)) {
            return ResponseEntity.ok(availableClassesProvider.getAll().stream()
                .toList());
        }

        return ResponseEntity.ok(employeeProvider.getByIdOrPersonalNumber(userId, personalNumber)
            .map(Employee::getPositionId)
            .map(availableClassesProvider::get)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new));
    }

}
