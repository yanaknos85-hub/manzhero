package ru.sber.transport.integrations.provider.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.integrations.LoggingExtension;
import ru.sber.transport.integrations.dto.ContractorInfoRequest;
import ru.sber.transport.integrations.mapper.CarLocationMapper;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;
import ru.sber.transport.integrations.service.CarLocationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarLocationProviderImplTest {
    
    @Mock
    private CarLocationMapper carLocationMapper;
    @Mock
    private CarLocationService carLocationService;
    @InjectMocks
    private CarLocationProviderImpl carLocationProvider;
    
    @Captor
    private ArgumentCaptor<ContractorInfoRequest> contractorInfoRequestCaptor;
    
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(CarLocationProviderImpl.class);
    
    @Test
    void getCarLocation() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var contractorInfo1 = new CarLocationMessage.ContractorInfo(
                "testUrl1",
                "testLogin1",
                "testPassword1",
                List.of("testId1", "testId2")
        );
        var contractorInfo2 = new CarLocationMessage.ContractorInfo(
                "testUrl2",
                "testLogin2",
                "testPassword2",
                List.of("testId3", "testId4")
        );
        var contractorRequest = Map.of(
                id1,
                contractorInfo1,
                id2,
                contractorInfo2
                                      );
        var contractorInfoRequest1 = new ContractorInfoRequest(
                "testUrl1",
                "testLogin1",
                "testPassword1",
                List.of("testId1", "testId2")
        );
        var contractorInfoRequest2 = new ContractorInfoRequest(
                "testUrl2",
                "testLogin2",
                "testPassword2",
                List.of("testId3", "testId4")
        );
        doReturn(contractorInfoRequest1).when(carLocationMapper).carLocationMessageToContractorInfoRequest(contractorInfo1);
        doReturn(contractorInfoRequest2).when(carLocationMapper).carLocationMessageToContractorInfoRequest(contractorInfo2);
        doNothing().when(carLocationService).getOrdersLocation(contractorInfoRequestCaptor.capture());
        carLocationProvider.getCarLocation(contractorRequest);
        
        var capturedRequests = contractorInfoRequestCaptor.getAllValues();
        assertThat(capturedRequests)
                .hasSize(2)
                .containsExactlyInAnyOrder(contractorInfoRequest1, contractorInfoRequest2);
        
        doThrow(new RuntimeException("some error")).when(carLocationService).getOrdersLocation(contractorInfoRequest1);
        carLocationProvider.getCarLocation(contractorRequest);
        
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(2);
        var firstEvent = LOGGING_EXTENSION.getEvents().getFirst();
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        
        assertThat(firstEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(firstEvent.getMessage()).isEqualTo("some error");
        assertThat(secondEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(secondEvent.getFormattedMessage()).isEqualTo("Error while getting car location for contractor: %s with orders: %s".formatted(
                contractorInfo1.url(),
                contractorInfo1.orderPartnerIds()
                                                                                                                                               ));
    }
}