package ru.sber.transport.fraud.monitoring.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sber.transport.fraud.monitoring.model.Fraud;
import ru.sber.transport.fraud.monitoring.model.TripRequestData;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.grpc.FraudCheckGrpc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("FraudCheckGrpc")
class FraudCheckGrpcTest {

    private TripRequestsDatabaseProvider tripRequestsDatabaseProvider;
    private FraudsDatabaseProvider fraudsDatabaseProvider;
    private FraudCheckGrpc grpc;

    @BeforeEach
    void setup() {
        tripRequestsDatabaseProvider = mock(TripRequestsDatabaseProvider.class);
        fraudsDatabaseProvider = mock(FraudsDatabaseProvider.class);
        grpc = new FraudCheckGrpc(tripRequestsDatabaseProvider, fraudsDatabaseProvider);
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если заявка не найдена")
    void shouldReturnEmptyWhenTripRequestNotFound() {
        val requestId = UUID.randomUUID();
        val request = FraudInfoRequest.newBuilder()
                .setRequestId(requestId.toString())
                .build();

        when(tripRequestsDatabaseProvider.get(requestId)).thenReturn(Optional.empty());

        val responseObserver = new TestStreamObserver<FraudInfoResponse>();
        grpc.getFraudInfo(request, responseObserver);

        assertThat(responseObserver.isCompleted()).isTrue();
        assertThat(responseObserver.getError()).isNull();
        assertThat(responseObserver.getResponse().getRecordsList()).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть записи о мошенничестве, если заявка найдена и мошенничества есть")
    void shouldReturnFraudRecordsWhenTripRequestFound() {
        val requestId = UUID.randomUUID();
        val passengerId = UUID.randomUUID();
        val fraudId1 = UUID.randomUUID();
        val fraudId2 = UUID.randomUUID();

        val request = FraudInfoRequest.newBuilder()
                .setRequestId(requestId.toString())
                .build();

        val tripRequest = mock(TripRequestData.class);
        val passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        when(tripRequest.getPassenger()).thenReturn(passenger);
        when(tripRequest.getHumanReadableId()).thenReturn("OT-11111");
        when(tripRequestsDatabaseProvider.get(requestId)).thenReturn(Optional.of(tripRequest));

        val fraud1 = mock(Fraud.class);
        when(fraud1.getId()).thenReturn(fraudId1);
        when(fraud1.getRequestId()).thenReturn(requestId);
        when(fraud1.getComment()).thenReturn("Suspicious activity");
        when(fraud1.getFraudType()).thenReturn("ABSENCE");

        val fraud2 = mock(Fraud.class);
        when(fraud2.getId()).thenReturn(fraudId2);
        when(fraud2.getRequestId()).thenReturn(requestId);
        when(fraud2.getComment()).thenReturn("Fake document");
        when(fraud2.getFraudType()).thenReturn("ABSENCE");

        when(fraudsDatabaseProvider.findByRequestId(requestId)).thenReturn(List.of(fraud1, fraud2));

        val responseObserver = new TestStreamObserver<FraudInfoResponse>();
        grpc.getFraudInfo(request, responseObserver);

        assertThat(responseObserver.isCompleted()).isTrue();
        assertThat(responseObserver.getError()).isNull();
        FraudInfoResponse response = responseObserver.getResponse();
        assertThat(response.getRecordsList()).hasSize(2);

        val record1 = response.getRecords(0);
        assertThat(record1.getId()).isEqualTo(fraudId1.toString());
        assertThat(record1.getRequestId()).isEqualTo(requestId.toString());
        assertThat(record1.getHumanReadableId()).isEqualTo("OT-11111");
        assertThat(record1.getPassengerId()).isEqualTo(passengerId.toString());
        assertThat(record1.getComment()).isEqualTo("Suspicious activity");

        val record2 = response.getRecords(1);
        assertThat(record2.getId()).isEqualTo(fraudId2.toString());
        assertThat(record2.getPassengerId()).isEqualTo(passengerId.toString());
        assertThat(record2.getComment()).isEqualTo("Fake document");
    }

    @Test
    @DisplayName("Должен вернуть INVALID_ARGUMENT при неправильном формате UUID")
    void shouldReturnInvalidArgumentForMalformedUuid() {
        val invalidUuid = "not-a-uuid";
        FraudInfoRequest request = FraudInfoRequest.newBuilder()
                .setRequestId(invalidUuid)
                .build();

        val responseObserver = new TestStreamObserver<FraudInfoResponse>();
        grpc.getFraudInfo(request, responseObserver);

        assertThat(responseObserver.isCompleted()).isFalse();
        assertThat(responseObserver.getError()).isInstanceOf(StatusRuntimeException.class);
        val statusEx = (StatusRuntimeException) responseObserver.getError();
        assertThat(statusEx.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(statusEx.getStatus().getDescription()).contains("Invalid requestId format");
    }

    @Test
    @DisplayName("Должен вернуть INTERNAL при ошибке базы данных")
    void shouldReturnInternalOnDatabaseException() {
        val requestId = UUID.randomUUID();
        val request = FraudInfoRequest.newBuilder()
                .setRequestId(requestId.toString())
                .build();

        when(tripRequestsDatabaseProvider.get(requestId))
                .thenThrow(new RuntimeException("DB connection failed"));

        val responseObserver = new TestStreamObserver<FraudInfoResponse>();
        grpc.getFraudInfo(request, responseObserver);

        assertThat(responseObserver.isCompleted()).isFalse();
        assertThat(responseObserver.getError()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException statusEx = (StatusRuntimeException) responseObserver.getError();
        assertThat(statusEx.getStatus().getCode()).isEqualTo(Status.INTERNAL.getCode());
        assertThat(statusEx.getStatus().getDescription()).contains("Internal server error");
    }

    @Getter
    private static class TestStreamObserver<T> implements io.grpc.stub.StreamObserver<T> {

        private Throwable error;
        private T response;
        private boolean completed = false;

        @Override
        public void onNext(T value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}