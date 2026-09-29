package ru.sber.transport.tariff_fleet.service.grpc.impl;


import com.google.protobuf.BoolValue;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.tariff_fleet.grpc.dto.HasActiveByContractorIdRequest;
import ru.sber.transport.tariff_fleet.grpc.service.ContractsServiceGrpc;
import ru.sber.transport.tariff_fleet.service.ContractService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ContractsGrpcServiceImpl extends ContractsServiceGrpc.ContractsServiceImplBase {

    private final ContractService contractService;

    @Override
    public void haveActiveContractsByContractorId(HasActiveByContractorIdRequest request,
                                                  StreamObserver<BoolValue> responseObserver) {
        try {
            responseObserver.onNext(BoolValue.of(
                    contractService.haveActiveContractsByContractorId(UUID.fromString(request.getContractorId()))));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while checking active contracts by contractorId={}", request.getContractorId(), e);
            responseObserver.onError(e);
        }
    }

}