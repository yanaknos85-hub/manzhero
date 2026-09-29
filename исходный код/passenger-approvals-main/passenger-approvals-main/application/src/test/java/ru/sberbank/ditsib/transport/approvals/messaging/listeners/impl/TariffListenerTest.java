package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.TariffRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TariffMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Тест слушателя Тарифов")
@Transactional
@Disabled("Требуется актуализация")
class TariffListenerTest extends KafkaTest {
    
    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    @Qualifier("tariffInput")
    private Consumer<Message<TariffMessage>> tariffInput;
    
    private final TestSharedData sharedData = new TestSharedData();
    
    @AfterEach
    void clear() {
        tariffRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Создание тарифа - успех")
    @Disabled("Требуется актуализация")
    void handleTest_createTariffs() {
        //сгенерируем новое сообщение о тарифе
        TariffMessage message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, CARSHARING_ID, CONTRACT1_ID,
                                                    false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(1);
        Tariff tariff = tariffRepository.findAll().get(0);
        sharedData.checkTariff(tariff, message);
        
        //сгенерируем еще одно сообщение
        message = createTariffMessage(TARIFF2_ID, REGION2_ID, ORGANIZATION_2_ID, CARSHARING_ID, CONTRACT2_ID, false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(tariffRepository.count()).isEqualTo(2);
        tariff = tariffRepository.findAll().stream()
                                 .filter(t -> t.getRegionId().equals(REGION2_ID)).findFirst()
                                 .orElse(null);
        sharedData.checkTariff(tariff, message);
    }
    
    @Test
    @DisplayName("Редактирование тарифа - успех")
    @Disabled("Требуется актуализация")
    void handleTest_updateTariff() {
        //сгенерируем новое сообщение о тарифе
        TariffMessage message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, CARSHARING_ID, CONTRACT1_ID,
                                                    false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на коррекцию тарифа
        message = sharedData.createTariffMessage(TARIFF1_ID, REGION2_ID, ORGANIZATION_1_ID, message.getHumanReadableId(),
                                                     CARSHARING_ID, CONTRACT1_ID, false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        sharedData.checkTariff(tariffRepository.findAll().get(0), message);
    }
    
    @Test
    @DisplayName("Удаление тарифа - успех")
    @Disabled("Требуется актуализация")
    void handleTest_deleteTariff() {
        //сгенерируем новое сообщение о тарифе
        TariffMessage message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, CARSHARING_ID, CONTRACT1_ID,
                                                    false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
    
        //сгенерируем сообщение на удаление тарифа
        message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, CARSHARING_ID, CONTRACT1_ID, true);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(0);
    }
    
    @Disabled
    @Test
    @DisplayName("Попытка удаления несуществующего тарифа")
    void handleTest_deleteIncorrectTariff() {
        TariffMessage message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, CARSHARING_ID, CONTRACT1_ID, true);
        Throwable ex = catchThrowable(() -> tariffInput.accept(MessageBuilder.withPayload(message).build()));
        assertThat(ex.getCause() instanceof EntityNotFoundException).isTrue();
        assertThat(tariffRepository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Приход тарифов для других типов транспорта")
    @Disabled("Требуется актуализация")
    void handleTest_ignoreOtherTariffs() {
        //попробуем записать тарифы для других типов транспорта
        TariffMessage message = createTariffMessage(TARIFF1_ID, REGION1_ID, ORGANIZATION_1_ID, TAXI_ID, CONTRACT1_ID, false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
    
        message = createTariffMessage(TARIFF2_ID, REGION1_ID, ORGANIZATION_1_ID, PERSONAL_ID, CONTRACT2_ID, false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(2);
        
        message = createTariffMessage(TARIFF3_ID, REGION2_ID, ORGANIZATION_2_ID, PUBLIC_ID, CONTRACT3_ID, false);
        tariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(3);
    }
    
    /**
     * Сгенерировать сообщение о Тарифе
     * @param tariffId ID тарифа
     * @param regionId ID геозоны
     * @param organizationId ID корп.клиента
     * @param transportTypeId ID типа транспорта
     * @param contractId ID контракта
     * @param deleted флаг удаения тарифа
     * @return TariffMessage
     */
    private TariffMessage createTariffMessage(UUID tariffId, UUID regionId, UUID organizationId, UUID transportTypeId,
                                              UUID contractId, boolean deleted) {
        return sharedData.createTariffMessage(tariffId, regionId, organizationId, null, transportTypeId,
                                              contractId, deleted);
    }
}