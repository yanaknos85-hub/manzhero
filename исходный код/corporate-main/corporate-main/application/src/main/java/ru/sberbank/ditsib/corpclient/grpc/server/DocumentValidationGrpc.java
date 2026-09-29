package ru.sberbank.ditsib.corpclient.grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.grpc.service.*;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;

import ru.sberbank.ditsib.corpclient.service.EmployeeDocumentService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
public class DocumentValidationGrpc extends DocumentValidationServiceGrpc.DocumentValidationServiceImplBase {

    private static final String ERROR_NO_ACCESS_TO_CAR = "NO_ACCESS_TO_CAR";
    private static final String ERROR_LICENSE_EXPIRED = "LICENSE_EXPIRED";
    private static final String ERROR_OSAGO_EXPIRED = "OSAGO_EXPIRED";

    private final EmployeeDocumentService employeeDocumentService;

    public DocumentValidationGrpc(EmployeeDocumentService employeeDocumentService) {
        this.employeeDocumentService = employeeDocumentService;
    }

    @Override
    public void validateDocuments(ValidateDocumentsRequest request,
                                  StreamObserver<ValidateDocumentsResponse> responseObserver) {
        try {
            var employeeId = UUID.fromString(request.getEmployeeId());
            var carId = UUID.fromString(request.getCarId());
            var desiredDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(
                            request.getDesiredDate().getSeconds(),
                            request.getDesiredDate().getNanos()
                    ),
                    ZoneOffset.UTC
            );
            var colleagueEmployeeId = request.hasColleagueEmployeeId()
                    ? UUID.fromString(request.getColleagueEmployeeId())
                    : null;
            var employeeIdToCheck = colleagueEmployeeId != null ? colleagueEmployeeId : employeeId;

            List<String> errors = new ArrayList<>();

            var hasAccess = employeeDocumentService.hasEmployeeAccessToCar(employeeIdToCheck, carId);
            if (!hasAccess) {
                errors.add(ERROR_NO_ACCESS_TO_CAR);
            }
            var documents = employeeDocumentService.findValidationDocuments(employeeIdToCheck, carId, desiredDate);

            var hasDriverLic = false;
            var hasOsago = false;

            if (documents != null) {
                for (EmployeeDocument doc : documents) {
                    if (doc.getDocumentType().getDocumentCode() == DocumentCode.DRIVER_LIC) {
                        hasDriverLic = true;
                    } else if (doc.getDocumentType().getDocumentCode() == DocumentCode.OSAGO) {
                        hasOsago = true;
                    }
                }
            }
            if (!hasDriverLic) {
                errors.add(ERROR_LICENSE_EXPIRED);
            }
            if (!hasOsago) {
                errors.add(ERROR_OSAGO_EXPIRED);
            }

            var valid = hasAccess && hasDriverLic && hasOsago;

            ValidateDocumentsResponse response =
                    ValidateDocumentsResponse.newBuilder()
                            .setValid(valid)
                            .addAllErrors(errors)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Validation failed", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateDocumentsForCarSharing(ValidateDocumentsForCarSharingRequest request,
                                               StreamObserver<ValidateDocumentsForCarSharingResponse> responseObserver) {
        try {
            var employeeId = UUID.fromString(request.getEmployeeId());
            var desiredDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(
                            request.getDesiredDate().getSeconds(),
                            request.getDesiredDate().getNanos()
                    ),
                    ZoneOffset.UTC            );

            var hasActiveDriverLicense = employeeDocumentService.hasEmployeeActiveDriverLicense(employeeId, desiredDate);

            var responseBuilder = ValidateDocumentsForCarSharingResponse.newBuilder()
                    .setValid(hasActiveDriverLicense);
            if (!hasActiveDriverLicense) {
                responseBuilder.setError(ERROR_LICENSE_EXPIRED);
            }
            var response = responseBuilder.build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Validation failed", e);
            responseObserver.onError(e);
        }
    }
}
