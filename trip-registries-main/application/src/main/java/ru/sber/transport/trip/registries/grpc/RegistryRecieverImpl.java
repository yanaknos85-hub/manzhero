package ru.sber.transport.trip.registries.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.http.HttpStatusCode;
import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;
import ru.sber.transport.trip.registries.api.dto.RegistryItem;
import ru.sber.transport.trip.registries.api.service.ApiService;
import ru.sber.transport.trip.registries.database.dao.RegistryRepository;
import ru.sber.transport.trip.registries.database.model.RegistryState;
import ru.sber.transport.trip.registries.mapper.RegistryMapper;
import ru.sber.transport.trip.registries.messaging.senders.RegistrySender;
import ru.sber.transport.trip_registries.grpc.dto.RegistryDescriptor;
import ru.sber.transport.trip_registries.grpc.service.RegistryServiceGrpc;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Реализация запуска планировщиков.
 */
@Slf4j
@RequiredArgsConstructor
@GrpcService
class RegistryRecieverImpl extends RegistryServiceGrpc.RegistryServiceImplBase {

    private final ApiService apiService;
    private final RegistryMapper registryMapper;
    private final RegistryRepository registryRepository;
    private final RegistrySender registrySender;

    @Override
    public void receiveRegistry(RegistryDescriptor.Registry request,
                                StreamObserver<RegistryDescriptor.RegistryResponse> responseObserver) {

        if (request.getTripCount() == 0) {
            log.warn("receiveRegistry: no trips found!");
            responseObserver.onNext(RegistryDescriptor.RegistryResponse.newBuilder().setResult(0).build());
            responseObserver.onCompleted();
            return;
        }

        try {
            var uuid = UUID.fromString(request.getRegistryId());
            var registry = registryRepository.findById(uuid).orElseGet(() -> createRegistry(uuid));
            registry.setOrganizationId(request.getOrganizationId());
            registry.setContractId(request.getContractIdUVHD());
            registry.setStartDate(request.getStartDate());
            registry.setEndDate(request.getEndDate());
            registry.setRegistryState(RegistryState.INIT.name());
            registry.setUpdStatus(0);
            registry.setServiceType(request.getServiceType());
            log.info("receiveRegistry: registry received with id " + registry.getRegistryId());

            var registryRequest = registryMapper.toApi(request);
            var tripList = new ArrayList<RegistryItem>();
            for (var grpcRegistryItem : request.getTripList()) {
                var registryItem = registryMapper.toApi(grpcRegistryItem);
                tripList.add(registryItem);
            }

            registryRequest.getTrip().addAll(tripList);
            HttpStatusCode statusResponse = null;
            try {
                statusResponse = apiService.sendRegistry(registryRequest);
            } catch (Exception e) {
                log.error("postRegistry: sendRegistry: exception: ", e);
                registry.setLastError(e.toString());
            }
            if (statusResponse != null && statusResponse.value() == 200) {
                responseObserver.onNext(RegistryDescriptor.RegistryResponse.newBuilder().setResult(1).build());
                registry.setRegistryState(RegistryState.REGISTRY_SENT_SUCCESS.name());
            } else {
                log.error("receiveRegistry: status = " + statusResponse);
                responseObserver.onNext(RegistryDescriptor.RegistryResponse.newBuilder().setResult(0).build());
                registry.setRegistryState(RegistryState.REGISTRY_SENDING_FAILED.name());
            }

            registryRepository.save(registry);
            responseObserver.onCompleted();
            log.info("receiveRegistry: registry saved with status " + registry.getRegistryState());
        } catch (Exception e) {
            log.error("postRegistry exception: ", e);
            responseObserver.onError(e);
        }
    }

    RegistryRecord createRegistry(UUID registryId) {
        var registry = new RegistryRecord();
        registry.setRegistryId(registryId);
        return registry;
    }
}
