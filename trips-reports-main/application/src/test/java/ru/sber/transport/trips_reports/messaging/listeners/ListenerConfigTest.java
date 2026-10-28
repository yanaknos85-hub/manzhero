package ru.sber.transport.trips_reports.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sber.transport.trips_reports.messaging.mapper.MessageMapperImpl;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.TripStatus;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Проверка слушателей")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_reports")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Проверка получения отчетов")
    void test_request() {
        var tripProvider = mock(TripProvider.class);

        var messageMapper = new MessageMapperImpl();

        var message = Instancio.of(TripReportMessage.class)
                .set(Select.field(TripReportMessage::getStatus), TripStatus.ORDER_FINISHED.getRusName())
                .create();
        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY, message.getId());
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.tripReportsInput(tripProvider, messageMapper);

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(ReportRecord.class);

        verify(tripProvider).save(messageCaptor.capture());

        var actual = messageCaptor.getValue();

        Assertions.assertEquals(actual.getContractorId(), message.getContractorId());
        Assertions.assertEquals(actual.getTripHumanReadableId(), message.getTripHumanReadableId());
        Assertions.assertEquals(actual.getStatus(), message.getStatus());
    }
}