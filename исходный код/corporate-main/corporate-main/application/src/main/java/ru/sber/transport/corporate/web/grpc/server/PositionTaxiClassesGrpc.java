package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.grpc.service.PositionTaxiClassesGrpc.PositionTaxiClassesImplBase;
import ru.sber.transport.corporate.grpc.service.TaxiClasses.EmployeeIdInfoRequest;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class PositionTaxiClassesGrpc extends PositionTaxiClassesImplBase {

    private final EmployeeProvider employeeProvider;

    private final AvailableClassesProvider availableClassesProvider;

    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
    );

    @Override
    public void allForEmployee(EmployeeIdInfoRequest request,
        StreamObserver<StringValue> responseObserver) {
        log.info("Requested all taxi classess for employee");
        try {
            val employeeId = request.hasId() ? Optional.of(request.getId().getValue())
                .filter(StringUtils::isNotBlank)
                .filter(id -> UUID_PATTERN.matcher(id).matches())
                .map(UUID::fromString)
                .orElse(null) : null;
            val personalNumber = request.hasPersonnelNumber() ? request.getPersonnelNumber().getValue() : null;
            if (Objects.isNull(employeeId) && StringUtils.isBlank(personalNumber)) {
                availableClassesProvider.getAll().stream()
                    .map(StringValue::of)
                    .forEach(responseObserver::onNext);
            } else {
                employeeProvider.getByIdOrPersonalNumber(employeeId, personalNumber)
                    .map(Employee::getPositionId)
                    .map(availableClassesProvider::get)
                    .map(ArrayList::new)
                    .orElseGet(ArrayList::new)
                    .stream()
                    .map(StringValue::of)
                    .forEach(responseObserver::onNext);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }

}
