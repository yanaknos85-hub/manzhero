package ru.sber.transport.integrations.messaging.listeners;

import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.transport.integrations.config.ContractorBlockProperties;
import ru.sber.transport.integrations.dto.OrderRequest;
import ru.sber.transport.integrations.feign.OrderClient;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;
import ru.sber.transport.integrations.messaging.sender.InContractorTaxiTripInProgressSender;
import ru.sber.transport.integrations.provider.CarLocationProvider;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.IntegrationParamsDTO;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sber.transport.request.messaging.Waypoint;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EmbeddedPostgres
@SpringBootTest(properties = { "SCHEDULED_API=*/2 * * * * *", "extlogging.kafka: false"})
@EnableWireMock({
        @ConfigureWireMock(name = "disp-ext-server", port = 65432, property = "spring.cloud.openfeign.client.config.dist-ext-client.url")
})
class ListenerConfigTest extends KafkaTest {
    @MockitoBean
    @SuppressWarnings("unused")
    private CarLocationProvider carLocationProvider;
    @MockitoSpyBean
    @SuppressWarnings("unused")
    private InContractorTaxiTripInProgressSender inProgressSender;
    @MockitoSpyBean
    @SuppressWarnings("unused")
    private OrderClient orderClient;
    @MockitoBean
    @SuppressWarnings("unused")
    private ContractorBlockProperties contractorBlockProperties;
    @Captor
    private ArgumentCaptor<Map<UUID, CarLocationMessage.ContractorInfo>> contractorInfoMapCaptor;

    @Test
    void carLocationInput() {
        doNothing().when(carLocationProvider).getCarLocation(contractorInfoMapCaptor.capture());

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
        var message = new CarLocationMessage(
                UUID.randomUUID(),
                Map.of(
                        id1,
                        contractorInfo1,
                        id2,
                        contractorInfo2
                      )
        );

        produceMessage("service.request.car-location-request", message);

        var capturedMap = contractorInfoMapCaptor.getValue();
        capturedMap.forEach((key, value) -> {
            if (key.equals(id1)) {
                assertThat(value)
                        .extracting(
                                CarLocationMessage.ContractorInfo::url,
                                CarLocationMessage.ContractorInfo::login,
                                CarLocationMessage.ContractorInfo::password,
                                CarLocationMessage.ContractorInfo::orderPartnerIds
                                   )
                        .containsExactly(
                                "testUrl1",
                                "testLogin1",
                                "testPassword1",
                                List.of("testId1", "testId2")
                                        );
            } else if (key.equals(id2)) {
                assertThat(value)
                        .extracting(
                                CarLocationMessage.ContractorInfo::url,
                                CarLocationMessage.ContractorInfo::login,
                                CarLocationMessage.ContractorInfo::password,
                                CarLocationMessage.ContractorInfo::orderPartnerIds
                                   )
                        .containsExactly(
                                "testUrl2",
                                "testLogin2",
                                "testPassword2",
                                List.of("testId3", "testId4")
                                        );
            }
        });
    }

    @SneakyThrows
    @Test
    void contractorTaxiTripInput() {
        var inTripTopic = "service.integrations.contractor.trip";
        var outTripTopic = "service.integrations.contractor.trip.inprogress";
        var integrationsParams = Instancio.of(IntegrationParamsDTO.class)
                                          .set(field(IntegrationParamsDTO::getContractorLogin), "SbXZSyniBBP")
                                          .set(field(IntegrationParamsDTO::getContractorPassword), "ATfZ81FUERp7rE7orTLvBy6FwOmsZCiqUqGNF13OyqY=")
                                          .set(field(IntegrationParamsDTO::getContractorUrl), "http://localhost:65432")
                                          .create();
        var messageForNewTrip = Instancio.of(OutContractorTaxiTripMessage.class)
                                         .set(field(OutContractorTaxiTripMessage::integrationType), TaxiExternalIntegrationType.JSON_API_1_0)
                                         .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                                         .set(field(OutContractorTaxiTripMessage::integrationParams), integrationsParams)
                                         .set(field(OutContractorTaxiTripMessage::waypoints), Instancio.ofList(Waypoint.class)
                                                                                                          .size(2)
                                                                                                          .create())
                                         .create();
        var messageForInfoTrip = Instancio.of(OutContractorTaxiTripMessage.class)
                                          .set(field(OutContractorTaxiTripMessage::integrationType), TaxiExternalIntegrationType.JSON_API_1_0)
                                          .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                                          .set(field(OutContractorTaxiTripMessage::integrationParams), integrationsParams)
                                          .set(field(OutContractorTaxiTripMessage::waypoints), Instancio.ofList(Waypoint.class)
                                                                                                           .size(2)
                                                                                                           .create())
                                          .create();
        var messageForCancelTrip = Instancio.of(OutContractorTaxiTripMessage.class)
                                            .set(field(OutContractorTaxiTripMessage::integrationType), TaxiExternalIntegrationType.JSON_API_1_0)
                                            .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.REJECT)
                                            .set(field(OutContractorTaxiTripMessage::integrationParams), integrationsParams)
                                            .set(field(OutContractorTaxiTripMessage::waypoints), Instancio.ofList(Waypoint.class)
                                                                                                             .size(2)
                                                                                                             .create())
                                            .create();
        produceMessage(inTripTopic, messageForCancelTrip);
        produceMessage(inTripTopic, messageForInfoTrip);
        produceMessage(inTripTopic, messageForNewTrip);
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(inProgressSender, times(3)).send(any()));
        var responseMessages = consumeMessages(outTripTopic, InContractorTaxiTripInProgressMessage.class);
        assertThat(responseMessages).hasSize(3);
        var responseMessageForNewTrip = responseMessages.stream()
                                                        .filter(message -> message.taxiId() != null &&
                                                                           message.taxiId().equals("503f4f4d-a59a-4665-b785-99ac94120520"))
                                                        .findFirst()
                                                        .orElseThrow();
        var responseMessageForInfoTrip = responseMessages.stream()
                                                         .filter(message -> message.tripId() != null &&
                                                                            message.tripId().equals(messageForInfoTrip.tripId()))
                                                         .findFirst()
                                                         .orElseThrow();
        var responseMessageForCancelTrip = responseMessages.stream()
                                                           .filter(message -> message.tripId() != null &&
                                                                              message.tripId().equals(messageForCancelTrip.tripId()))
                                                           .findFirst()
                                                           .orElseThrow();
        assertThat(responseMessageForNewTrip)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields("taxiId", "status", "integrationType")
                .isEqualTo(messageForNewTrip);
        assertThat(responseMessageForNewTrip.taxiId()).isEqualTo("503f4f4d-a59a-4665-b785-99ac94120520");
        assertThat(responseMessageForNewTrip.integrationType()).isEqualTo(TaxiExternalIntegrationType.JSON_API_1_0);
        assertThat(responseMessageForNewTrip.status()).isEqualTo(InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(responseMessageForInfoTrip)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields("taxiId", "status", "integrationType", "distance")
                .isEqualTo(messageForInfoTrip);
        assertThat(responseMessageForInfoTrip.taxiId()).isNull();
        assertThat(responseMessageForInfoTrip.integrationType()).isEqualTo(TaxiExternalIntegrationType.JSON_API_1_0);
        assertThat(responseMessageForInfoTrip.status()).isEqualTo(InboundTaxiTripStatus.DRIVER_ON_THE_WAY);
        assertThat(responseMessageForInfoTrip.distance()).isEqualTo(Double.valueOf(4991.93));
        assertThat(responseMessageForCancelTrip)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields("taxiId", "status", "integrationType", "distance")
                .isEqualTo(messageForCancelTrip);
        assertThat(responseMessageForCancelTrip.taxiId()).isNull();
        assertThat(responseMessageForCancelTrip.integrationType()).isEqualTo(TaxiExternalIntegrationType.JSON_API_1_0);
        assertThat(responseMessageForCancelTrip.status()).isEqualTo(InboundTaxiTripStatus.DRIVER_ON_THE_WAY);
        assertThat(responseMessageForCancelTrip.distance()).isEqualTo(Double.valueOf(4991.93));
        verify(orderClient).createOrder(any(URI.class), anyString(), anyString(), anyString(), any(OrderRequest.class));
        verify(orderClient, times(2)).orderInfo(any(URI.class), anyString(), anyString(), anyString(), anyString());
        verify(orderClient).cancel(any(URI.class), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
