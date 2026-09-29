package ru.sber.transport.tariff_fleet.service.grpc.impl;

import com.google.protobuf.BoolValue;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.grpc.dto.HasActiveByContractorIdRequest;
import ru.sber.transport.tariff_fleet.service.ContractService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractsGrpcServiceImplTest {

    @InjectMocks
    private ContractsGrpcServiceImpl contractsGrpcService;
    @Mock
    private ContractService contractService;
    @Mock
    private StreamObserver<BoolValue> boolValueStreamObserver;


    @Test
    void haveActiveContractsByContractorId() {
        var contractorId1 = UUID.randomUUID();
        var contractorId2 = UUID.randomUUID();
        var request1 = HasActiveByContractorIdRequest.newBuilder().setContractorId(contractorId1.toString()).build();
        var request2 = HasActiveByContractorIdRequest.newBuilder().setContractorId(contractorId2.toString()).build();

        doReturn(true).when(contractService).haveActiveContractsByContractorId(contractorId1);
        doNothing().when(boolValueStreamObserver).onNext(any());
        doNothing().when(boolValueStreamObserver).onCompleted();
        doThrow(JUnitException.class).when(contractService).haveActiveContractsByContractorId(contractorId2);
        doNothing().when(boolValueStreamObserver).onError(any(JUnitException.class));

        contractsGrpcService.haveActiveContractsByContractorId(request1, boolValueStreamObserver);
        contractsGrpcService.haveActiveContractsByContractorId(request2, boolValueStreamObserver);

        verify(boolValueStreamObserver).onNext(any());
        verify(boolValueStreamObserver).onCompleted();
        verify(boolValueStreamObserver).onError(any(JUnitException.class));
    }
}
