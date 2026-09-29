package ru.sberbank.ditsib.corpclient.messaging.initializations;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;
import ru.sberbank.ditsib.corpclient.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка инициализатора топика целей поездок")
class PurposeInitializerTest {

    private final TripPurposeRepository repository = mock(TripPurposeRepository.class);

    private final PurposeSender sender = mock(PurposeSender.class);

    private final ObjectProvider<OutputBridge> outputBridges = new SimpleObjectProvider<>(mock(OutputBridge.class));

    @Test
    @DisplayName("Получение канала")
    void test_getChannel() {
        var init = new PurposeInitializer(repository, sender, outputBridges, new SimpleObjectProvider<>(mock(OutputBridge.class)), new SimpleObjectProvider<>(mock(OutputBridge.class)));

        assertThat(init.channel()).isEqualTo(outputBridges.getObject());
    }

    @Test
    @DisplayName("Получение канала SSL")
    void test_getChannel_ssl() {
        var init = new PurposeInitializer(repository, sender, new SimpleObjectProvider<>(null), outputBridges, new SimpleObjectProvider<>(mock(OutputBridge.class)));

        assertThat(init.channel()).isEqualTo(outputBridges.getObject());
    }

    @Test
    @DisplayName("Получение канала AVRO")
    void test_getChannel_avro() {
        var init = new PurposeInitializer(repository, sender, new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(null), outputBridges);

        assertThat(init.channel()).isEqualTo(outputBridges.getObject());
    }

    @Test
    @DisplayName("Инициализация")
    void test_initialize() {
        var init = new PurposeInitializer(repository, sender, new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(null), outputBridges);

        var list = Instancio.createList(TripPurpose.class);

        when(repository.findAll()).thenReturn(list);

        init.initialize();

        var activeCaptor = ArgumentCaptor.forClass(TripPurpose.class);

        verify(sender, times(list.size())).send(activeCaptor.capture());

        assertThat(activeCaptor.getAllValues()).hasSameElementsAs(list);
    }

}