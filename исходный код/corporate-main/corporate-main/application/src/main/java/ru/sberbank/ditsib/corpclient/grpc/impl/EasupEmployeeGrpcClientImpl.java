package ru.sberbank.ditsib.corpclient.grpc.impl;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync.grpc.service.StateServiceGrpc;
import ru.sberbank.ditsib.corpclient.grpc.EasupEmployeeGrpcClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class EasupEmployeeGrpcClientImpl implements EasupEmployeeGrpcClient {

    @GrpcClient("easup")
    private StateServiceGrpc.StateServiceBlockingStub stub;

    @Override
    public State.Employee getEmployee(String personnelNumber) {
        try {
            return stub.getEmployee(
                    State.Request.newBuilder()
                            .setId(personnelNumber)
                            .build()
            );
        } catch (StatusRuntimeException ex) {
            log.error("Failed to fetch employee from EASUP", ex.getMessage());
            return null;
        }
    }
}
