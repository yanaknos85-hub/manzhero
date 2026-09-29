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
import ru.sberbank.ditsib.corpclient.database.dao.DelegateRepository;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.corpclient.messaging.sender.DelegateSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка инициализатора топика делегатов")
class DelegateTopicInitializationTest {

    private final DelegateRepository repository = mock(DelegateRepository.class);

    private final DelegateSender sender = mock(DelegateSender.class);

    private final ObjectProvider<OutputBridge> delegateOutput = new SimpleObjectProvider<>(mock(OutputBridge.class));

    @Test
    @DisplayName("Получение канала")
    void test_getChannel() {
        var init = new DelegateTopicInitialization(repository, sender, delegateOutput, new SimpleObjectProvider<>(mock(OutputBridge.class)), new SimpleObjectProvider<>(mock(OutputBridge.class)));

        assertThat(init.channel()).isEqualTo(delegateOutput.getObject());
    }

    @Test
    @DisplayName("Получение канала SSL")
    void test_getChannel_ssl() {
        var init = new DelegateTopicInitialization(repository, sender, new SimpleObjectProvider<>(null), delegateOutput, new SimpleObjectProvider<>(mock(OutputBridge.class)));

        assertThat(init.channel()).isEqualTo(delegateOutput.getObject());
    }

    @Test
    @DisplayName("Получение канала AVRO")
    void test_getChannel_avro() {
        var init = new DelegateTopicInitialization(repository, sender, new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(null), delegateOutput);

        assertThat(init.channel()).isEqualTo(delegateOutput.getObject());
    }

    @Test
    @DisplayName("Инициализация")
    void test_initialize() {
        var init = new DelegateTopicInitialization(repository, sender, new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(null), delegateOutput);

        var list = Instancio.createList(DelegateRecord.class);

        when(repository.findAll()).thenReturn(list);

        init.initialize();

        var activeCaptor = ArgumentCaptor.forClass(DelegateRecord.class);
        var inactiveCaptor = ArgumentCaptor.forClass(DelegateRecord.class);

        verify(sender, times((int) list.stream().filter(d -> RecordStatus.ACTIVE.equals(d.getStatus())).count())).send(activeCaptor.capture());
        verify(sender, times((int) list.stream().filter(d -> RecordStatus.INACTIVE.equals(d.getStatus())).count())).sendDelete(inactiveCaptor.capture());

        assertThat(activeCaptor.getAllValues()).hasSameElementsAs(list.stream().filter(d -> RecordStatus.ACTIVE.equals(d.getStatus())).toList());
        assertThat(inactiveCaptor.getAllValues()).hasSameElementsAs(list.stream().filter(d -> RecordStatus.INACTIVE.equals(d.getStatus())).toList());
    }

}