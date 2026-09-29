package ru.sber.transport.tariff_fleet.service.grpc.impl;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.fuel.grpc.dto.DeactivateFuelCardByContractAndDepartmentIdRequest;
import ru.sber.transport.fuel.grpc.dto.DeactivateFuelCardByContractIdRequest;
import ru.sber.transport.fuel.grpc.service.TariffServiceGrpc;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardContractException;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardTariffException;

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class FuelGrpcServiceImplTest {
    @InjectMocks
    private FuelGrpcServiceImpl fuelGrpcService;
    @Mock
    TariffServiceGrpc.TariffServiceBlockingStub stub;

    @Test
    public void deactivateFuelCardByContractId() {
        var contractId1 = UUID.randomUUID();
        var contractId2 = UUID.randomUUID();
        var dto1 = DeactivateFuelCardByContractIdRequest.newBuilder().setContractId(contractId1.toString()).build();
        var dto2 = DeactivateFuelCardByContractIdRequest.newBuilder().setContractId(contractId2.toString()).build();
        var mockException = new StatusRuntimeException(
                Status.NOT_FOUND
        );

        doReturn(Empty.getDefaultInstance()).when(stub).deactivateFuelCardByContractId(dto1);
        doThrow(mockException).when(stub).deactivateFuelCardByContractId(dto2);

        assertDoesNotThrow(() -> fuelGrpcService.deactivateFuelCardByContractId(contractId1));
        assertThrows(
                DeactivateFuelCardContractException.class,
                () -> fuelGrpcService.deactivateFuelCardByContractId(contractId2)
        );
    }

    @Test
    public void deactivateFuelCardByContractAndDepartmentId() {
        var contractId1 = UUID.randomUUID();
        var departmentId1 = UUID.randomUUID();
        var contractId2 = UUID.randomUUID();
        var departmentId2 = UUID.randomUUID();
        var dto1 = DeactivateFuelCardByContractAndDepartmentIdRequest.newBuilder()
                .setContractId(contractId1.toString())
                .setDepartmentId(departmentId1.toString())
                .build();
        var dto2 = DeactivateFuelCardByContractAndDepartmentIdRequest.newBuilder()
                .setContractId(contractId2.toString())
                .setDepartmentId(departmentId2.toString())
                .build();
        var mockException = new StatusRuntimeException(
                Status.NOT_FOUND
        );

        doReturn(Empty.getDefaultInstance()).when(stub).deactivateFuelCardByContractAndDepartmentId(dto1);
        doThrow(mockException).when(stub).deactivateFuelCardByContractAndDepartmentId(dto2);

        assertDoesNotThrow(() -> fuelGrpcService.deactivateFuelCardByContractAndDepartmentId(contractId1, departmentId1));
        assertThrows(
                DeactivateFuelCardTariffException.class,
                () -> fuelGrpcService.deactivateFuelCardByContractAndDepartmentId(contractId2, departmentId2)
        );
    }
}
