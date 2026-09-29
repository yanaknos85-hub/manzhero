package ru.sber.transport.tariff_fleet.service.grpc.impl;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Timestamp;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;

import java.time.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbGrpcServiceImplTest {

    @InjectMocks
    private EwbGrpcServiceImpl ewbGrpcService;
    @Mock
    private EwbServiceGrpc.EwbServiceBlockingStub ewbServiceBlockingStub;
    @Mock
    private Clock clock;

    private final Clock fixedClock = Clock.fixed(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()))
            .toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(ewbGrpcService, "ewbServiceBlockingStub", ewbServiceBlockingStub);
    }

    @Test
    void haveActiveEwb() {
        var departmentIds1 = Instancio.createList(UUID.class);
        var departmentIds2 = Instancio.createList(UUID.class);
        var startDate = LocalDate.now();
        var dto1 = Dto.HaveActiveEwbDto.newBuilder()
                .addAllDepartmentIds(departmentIds1.stream()
                        .map(UUID::toString)
                        .toList())
                .setCheckStartDate(Timestamp.newBuilder()
                        .setSeconds(startDate.atStartOfDay().toEpochSecond(ZoneOffset.of(fixedClock.getZone().getId())))
                        .setNanos(0).build())
                .build();
        var dto2 = Dto.HaveActiveEwbDto.newBuilder()
                .addAllDepartmentIds(departmentIds2.stream()
                        .map(UUID::toString)
                        .toList())
                .setCheckStartDate(Timestamp.newBuilder()
                        .setSeconds(startDate.atStartOfDay().toEpochSecond(ZoneOffset.of(fixedClock.getZone().getId())))
                        .setNanos(0).build())
                .build();

        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(BoolValue.of(true)).when(ewbServiceBlockingStub).haveActiveEwb(dto1);
        doReturn(BoolValue.of(false)).when(ewbServiceBlockingStub).haveActiveEwb(dto2);
        assertTrue(ewbGrpcService.haveActiveEwb(departmentIds1, startDate));
        assertFalse(ewbGrpcService.haveActiveEwb(departmentIds2, startDate));
        verify(ewbServiceBlockingStub, times(2)).haveActiveEwb(any());
    }
}