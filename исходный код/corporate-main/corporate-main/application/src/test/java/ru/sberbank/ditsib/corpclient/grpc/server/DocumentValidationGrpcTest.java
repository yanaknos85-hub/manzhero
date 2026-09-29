package ru.sberbank.ditsib.corpclient.grpc.server;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsForCarSharingRequest;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsForCarSharingResponse;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsRequest;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsResponse;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;

import ru.sberbank.ditsib.corpclient.service.EmployeeDocumentService;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка валидации документов")
class DocumentValidationGrpcTest {

    private final EmployeeDocumentService employeeDocumentService = mock(EmployeeDocumentService.class);

    private final DocumentValidationGrpc grpc = new DocumentValidationGrpc(employeeDocumentService);

    private UUID employeeId;
    private UUID carId;
    private Instant desiredDate;

    @BeforeEach
    void init() {
        employeeId = UUID.randomUUID();
        carId = UUID.randomUUID();
        desiredDate = Instant.now();
    }

    @Test
    @DisplayName("Валидация успешна - все документы есть и доступ есть")
    void test_validateDocuments_success() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        // Подготовка данных
        var driverLic = createEmployeeDocument(DocumentCode.DRIVER_LIC, true);
        var osago = createEmployeeDocument(DocumentCode.OSAGO, true);

        when(employeeDocumentService.hasEmployeeAccessToCar(any(), any())).thenReturn(true);
        when(employeeDocumentService.findValidationDocuments(any(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of(driverLic, osago));

        // Вызов
        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, null),
                createResponseObserver(actualResponse, error, completed)
        );

        // Проверка
        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isTrue();
        assertThat(response.getErrorsList()).isEmpty();
    }

    @Test
    @DisplayName("Валидация не удалась - нет доступа к автомобилю")
    void test_validateDocuments_noAccess() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(employeeDocumentService.hasEmployeeAccessToCar(any(), any())).thenReturn(false);
        when(employeeDocumentService.findValidationDocuments(any(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, null),
                createResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getErrorsList()).contains("NO_ACCESS_TO_CAR");
    }

    @Test
    @DisplayName("Валидация не удалась - ВУ не найдено/недействительно")
    void test_validateDocuments_licenseExpired() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var osago = createEmployeeDocument(DocumentCode.OSAGO, true);

        when(employeeDocumentService.hasEmployeeAccessToCar(any(), any())).thenReturn(true);
        when(employeeDocumentService.findValidationDocuments(any(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of(osago));

        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, null),
                createResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getErrorsList()).containsExactly("LICENSE_EXPIRED");
    }

    @Test
    @DisplayName("Валидация не удалась - ОСАГО не найдено/недействительно")
    void test_validateDocuments_osagoExpired() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var driverLic = createEmployeeDocument(DocumentCode.DRIVER_LIC, true);

        when(employeeDocumentService.hasEmployeeAccessToCar(any(), any())).thenReturn(true);
        when(employeeDocumentService.findValidationDocuments(any(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of(driverLic));

        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, null),
                createResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getErrorsList()).containsExactly("OSAGO_EXPIRED");
    }

    @Test
    @DisplayName("Валидация не удалась - оба документа не найдены")
    void test_validateDocuments_bothDocumentsExpired() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(employeeDocumentService.hasEmployeeAccessToCar(any(), any())).thenReturn(true);
        when(employeeDocumentService.findValidationDocuments(any(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, null),
                createResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getErrorsList()).containsExactlyInAnyOrder("LICENSE_EXPIRED", "OSAGO_EXPIRED");
    }

    @Test
    @DisplayName("Валидация для коллеги - colleagueEmployeeId заполнен")
    void test_validateDocuments_colleagueEmployeeId() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var colleagueId = UUID.randomUUID();
        var driverLic = createEmployeeDocument(DocumentCode.DRIVER_LIC, true);
        var osago = createEmployeeDocument(DocumentCode.OSAGO, true);

        when(employeeDocumentService.hasEmployeeAccessToCar(eq(colleagueId), any())).thenReturn(true);
        when(employeeDocumentService.findValidationDocuments(eq(colleagueId), any(), any(LocalDateTime.class)))
                .thenReturn(List.of(driverLic, osago));

        grpc.validateDocuments(
                createRequest(employeeId, carId, desiredDate, colleagueId),
                createResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isTrue();
        assertThat(response.getErrorsList()).isEmpty();

        // Проверяем, что были вызваны методы с colleagueId
        var accessCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(employeeDocumentService).hasEmployeeAccessToCar(accessCaptor.capture(), any());
        assertThat(accessCaptor.getValue()).isEqualTo(colleagueId);
    }

    private ValidateDocumentsRequest createRequest(UUID empId, UUID carId, Instant date, UUID colleagueId) {
        var desiredDateProto = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(date.getEpochSecond())
                .setNanos(date.getNano())
                .build();

        var builder = ValidateDocumentsRequest.newBuilder()
                .setEmployeeId(empId.toString())
                .setCarId(carId.toString())
                .setDesiredDate(desiredDateProto);

        if (colleagueId != null) {
            builder.setColleagueEmployeeId(colleagueId.toString());
        }

        return builder.build();
    }

    private StreamObserver<ValidateDocumentsResponse> createResponseObserver(
            CopyOnWriteArrayList<ValidateDocumentsResponse> responses,
            AtomicReference<Throwable> error,
            AtomicBoolean completed
    ) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ValidateDocumentsResponse value) {
                responses.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };
    }

    private EmployeeDocument createEmployeeDocument(DocumentCode code, boolean valid) {
        var documentType = new DocumentType(code, code.name());
        var doc = new EmployeeDocument();
        doc.setDocumentType(documentType);
        doc.setEmployeeId(employeeId);
        doc.setCarId(carId);

        if (valid) {
            doc.setStartTimeDocument(LocalDateTime.now().minusDays(1));
            doc.setFinalTimeDocument(LocalDateTime.now().plusYears(1));
        } else {
            doc.setStartTimeDocument(LocalDateTime.now().plusDays(1));
            doc.setFinalTimeDocument(LocalDateTime.now().plusYears(2));
        }

        return doc;
    }

    @Test
    @DisplayName("Валидация для каршаринга успешна - ВУ активно")
    void test_validateDocumentsForCarSharing_success() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsForCarSharingResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(employeeDocumentService.hasEmployeeActiveDriverLicense(any(), any(LocalDateTime.class)))
                .thenReturn(true);

        grpc.validateDocumentsForCarSharing(
                createCarSharingRequest(employeeId, desiredDate),
                createCarSharingResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isTrue();
        assertThat(response.getError()).isEmpty();
    }

    @Test
    @DisplayName("Валидация для каршаринга не удалась - ВУ неактивно")
    void test_validateDocumentsForCarSharing_licenseExpired() {
        var actualResponse = new CopyOnWriteArrayList<ValidateDocumentsForCarSharingResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(employeeDocumentService.hasEmployeeActiveDriverLicense(any(), any(LocalDateTime.class)))
                .thenReturn(false);

        grpc.validateDocumentsForCarSharing(
                createCarSharingRequest(employeeId, desiredDate),
                createCarSharingResponseObserver(actualResponse, error, completed)
        );

        assertThat(actualResponse).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        var response = actualResponse.getFirst();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getError()).isEqualTo("LICENSE_EXPIRED");
    }

    private ValidateDocumentsForCarSharingRequest createCarSharingRequest(UUID empId, Instant date) {
        var desiredDateProto = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(date.getEpochSecond())
                .setNanos(date.getNano())
                .build();

        return ValidateDocumentsForCarSharingRequest.newBuilder()
                .setEmployeeId(empId.toString())
                .setDesiredDate(desiredDateProto)
                .build();
    }

    private StreamObserver<ValidateDocumentsForCarSharingResponse> createCarSharingResponseObserver(
            CopyOnWriteArrayList<ValidateDocumentsForCarSharingResponse> responses,
            AtomicReference<Throwable> error,
            AtomicBoolean completed
    ) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ValidateDocumentsForCarSharingResponse value) {
                responses.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };
    }
}
