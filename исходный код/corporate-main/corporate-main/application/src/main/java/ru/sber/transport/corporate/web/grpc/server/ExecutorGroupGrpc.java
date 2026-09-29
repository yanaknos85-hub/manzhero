package ru.sber.transport.corporate.web.grpc.server;

import io.grpc.stub.StreamObserver;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.grpc.service.ExecutorGroup;
import ru.sber.transport.corporate.grpc.service.ExecutorGroupsGrpc;
import ru.sber.transport.corporate.web.grpc.mappers.ExecutorGroupGrpcMapper;
import ru.sberbank.ditsib.corpclient.service.ExecutorGroupService;

import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
class ExecutorGroupGrpc extends ExecutorGroupsGrpc.ExecutorGroupsImplBase {

    @Autowired
    private ExecutorGroupService service;

    @Autowired
    private ExecutorGroupGrpcMapper mapper;


    @Transactional
    @Override
    public void getExecutorGroupByEmployeeId(
            ExecutorGroup.GetExecutorGroupByEmployeeIdRequest request,
            StreamObserver<ExecutorGroup.GetExecutorGroupByEmployeeIdResponse> responseObserver) {
        log.info("Requested getExecutorGroupByEmployeeId {}", request.getEmployeeId());
        try {
            if (request.getEmployeeId() == null) {
                responseObserver.onError(new IllegalArgumentException("EmployeeId is null"));
            }
            UUID employeeId = UUID.fromString(request.getEmployeeId());
            List<UUID> geoZones = request.getGeoZoneIdsList()
                    .stream()
                    .map(UUID::fromString)
                    .toList();

            var result = service.getExecutorGroupByEmployeeId(
                    employeeId,
                    geoZones,
                    StringUtils.isNotBlank(request.getService())?request.getService() : "CARGO_TRANSPORTATION");
            responseObserver.onNext(mapper.toGrpc(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }


}
