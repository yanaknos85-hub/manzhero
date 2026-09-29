package ru.sber.transport.tariff_fleet.service.grpc.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;
import ru.sber.transport.tariff_fleet.service.grpc.EwbGrpcService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EwbGrpcServiceImpl implements EwbGrpcService {

    private final Clock clock;

    @GrpcClient("grpc-ewb")
    private EwbServiceGrpc.EwbServiceBlockingStub ewbServiceBlockingStub;

    @Override
    public boolean haveActiveEwb(List<UUID> departmentIds, LocalDate checkStartDate) {
        return ewbServiceBlockingStub.haveActiveEwb(Dto.HaveActiveEwbDto.newBuilder()
                .addAllDepartmentIds(departmentIds.stream()
                        .map(UUID::toString)
                        .toList())
                .setCheckStartDate(Timestamp.newBuilder()
                        .setSeconds(checkStartDate.atStartOfDay().toEpochSecond(ZoneOffset.of(clock.getZone().getId())))
                        .setNanos(0).build())
                .build()).getValue();
    }
}
