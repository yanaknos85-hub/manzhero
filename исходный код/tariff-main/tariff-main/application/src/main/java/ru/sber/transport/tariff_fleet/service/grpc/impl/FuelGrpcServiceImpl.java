package ru.sber.transport.tariff_fleet.service.grpc.impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.fuel.grpc.dto.DeactivateFuelCardByContractAndDepartmentIdRequest;
import ru.sber.transport.fuel.grpc.dto.DeactivateFuelCardByContractIdRequest;
import ru.sber.transport.fuel.grpc.service.TariffServiceGrpc;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardContractException;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardTariffException;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class FuelGrpcServiceImpl implements FuelGrpcService {
    @GrpcClient("grpc-fleet-fuel")
    private TariffServiceGrpc.TariffServiceBlockingStub tariffServiceBlockingStub;

    @Override
    public void deactivateFuelCardByContractId(UUID contractId) {
        try {
            var result = tariffServiceBlockingStub.deactivateFuelCardByContractId(
                    DeactivateFuelCardByContractIdRequest.newBuilder()
                            .setContractId(contractId.toString())
                            .build()
            );
        } catch (Exception e) {
            throw new DeactivateFuelCardContractException();
        }
    }

    @Override
    public void deactivateFuelCardByContractAndDepartmentId(UUID contractId, UUID departmentId) {
        try{
            var result = tariffServiceBlockingStub.deactivateFuelCardByContractAndDepartmentId(
                    DeactivateFuelCardByContractAndDepartmentIdRequest.newBuilder()
                            .setContractId(contractId.toString())
                            .setDepartmentId(departmentId.toString())
                            .build()
            );
        } catch (Exception e) {
            throw new DeactivateFuelCardTariffException();
        }
    }
}
