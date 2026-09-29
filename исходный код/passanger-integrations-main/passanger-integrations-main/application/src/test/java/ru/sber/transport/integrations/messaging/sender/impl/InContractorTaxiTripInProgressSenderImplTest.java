package ru.sber.transport.integrations.messaging.sender.impl;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.transport.integrations.config.ContractorBlockProperties;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.messaging.sender.InContractorTaxiTripInProgressSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.logging.model.LogField;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedPostgres
class InContractorTaxiTripInProgressSenderImplTest extends KafkaTest {
    @Autowired
    private InContractorTaxiTripInProgressSender InContractorTaxiTripInProgressSender;
    @MockitoSpyBean
    @Qualifier("inContractorTaxiTripInProgressOutput")
    private OutputBridge inContractorTaxiTripInProgressOutput;
    @MockitoBean
    @SuppressWarnings("unused")
    private ContractorBlockProperties contractorCacheProperties;
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @Test
    void send() {
        MDC.clear();
        MDC.put(LogField.TRACE_ID.getKey(), UUID.randomUUID().toString());
        var message = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        InContractorTaxiTripInProgressSender.send(message);
        var actual = consumeMessage("service.integrations.contractor.trip.inprogress", InContractorTaxiTripInProgressMessage.class);
        verify(inContractorTaxiTripInProgressOutput).send(any(InContractorTaxiTripInProgressMessage.class), mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getAllValues())
                .hasSize(1)
                .extracting(
                        Map::keySet
                )
                .containsExactlyInAnyOrder(
                        Set.of(KafkaHeaders.KEY, LogField.TRACE_ID.getKey())
                );
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                        .builder()
                        .withComparatorForType(Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                LocalDateTime.class)
                        .build())
                .isEqualTo(message);
    }
}
