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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.constants.TaxiClass.COMFORT;
import static ru.sberbank.ditsib.transport.constants.TaxiClass.ECONOMY;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Тест слушателя Тарифов")
@Transactional
@MockitoBean(types = {JwtDecoder.class})
class TaxiTariffListenerTest extends KafkaTest {
    
    @Autowired
    private TaxiTariffRepository tariffRepository;
    
    @Autowired
    @Qualifier("taxiTariffInput")
    private Consumer<Message<TaxiTariffMessage>> taxiTariffInput;
    
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
        TaxiTariffMessage message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, false);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(1);
        TaxiTariff tariff = tariffRepository.findAll().get(0);
        sharedData.checkTaxiTariff(tariff, message);
        
        //сгенерируем еще одно сообщение
        message = createTaxiTariffMessage(TARIFF2_ID, REGION2_ID, CONTRACT2_ID, ORGANIZATION_2_ID, COMFORT, false);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(tariffRepository.count()).isEqualTo(2);
        tariff = tariffRepository.findAll().stream()
                                 .filter(t -> t.getRegionId().equals(REGION2_ID)).findFirst()
                                 .orElse(null);
        sharedData.checkTaxiTariff(tariff, message);
    }
    
    @Test
    @DisplayName("Редактирование тарифа - успех")
    @Disabled("Требуется актуализация")
    void handleTest_updateTariff() {
        //сгенерируем новое сообщение о тарифе
        TaxiTariffMessage message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, false);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на коррекцию тарифа
        message = sharedData.createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, message.getHumanReadableId(), CONTRACT1_ID,
                ORGANIZATION_1_ID, ECONOMY, false);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        sharedData.checkTaxiTariff(tariffRepository.findAll().get(0), message);
    }
    
    @Test
    @DisplayName("Удаление тарифа - успех")
    @Disabled("Требуется актуализация")
    void handleTest_deleteTariff() {
        //сгенерируем новое сообщение о тарифе
        TaxiTariffMessage message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, false);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
    
        //сгенерируем сообщение на удаление тарифа
        message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, true);
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(0);
    }
    
    @Disabled
    @Test
    @DisplayName("Попытка удаления несуществующего тарифа")
    void handleTest_deleteIncorrectTariff() {
        TaxiTariffMessage message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, true);
        Throwable ex = catchThrowable(() -> taxiTariffInput.accept(MessageBuilder.withPayload(message).build()));
        assertThat(ex.getCause() instanceof EntityNotFoundException).isTrue();
        assertThat(tariffRepository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Приход тарифов такси с другими типами транспорта")
    @Disabled("Требуется актуализация")
    void handleTest_ignoreOtherTariffs() {
        //попробуем записать тарифы для других типов транспорта
        TaxiTariffMessage message = createTaxiTariffMessage(TARIFF1_ID, REGION1_ID, CONTRACT1_ID, ORGANIZATION_1_ID, ECONOMY, false);
        message.setTransportType(TransportTypeEnum.CARSHARING.name());
        taxiTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        TaxiTariff taxiTariff = tariffRepository.findAll().get(0);
        // проверим, что ошибочные данные перезаписаны с типо транспорта Такси
        assertThat(taxiTariff.getId()).isEqualTo(message.getId());
        assertThat(taxiTariff.getTransportType()).isNotEqualTo(TransportTypeEnum.CARSHARING);
        assertThat(taxiTariff.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
    }
    
    /**
     * Создать сообщение о тарифе такси. Пишется regionId, region не пишется
     * @param tariffId ID тарифа
     * @param regionId ID геозоны
     * @param contractId ID контракта
     * @param organizationId ID корп.клиента
     * @param taxiClass класс такси
     * @param delete флаг удаления
     * @return TaxiTariffMessage
     */
    private TaxiTariffMessage createTaxiTariffMessage(
            UUID tariffId, UUID regionId, UUID contractId, UUID organizationId, TaxiClass taxiClass, boolean delete) {
        return sharedData.createTaxiTariffMessage(tariffId, regionId, null, contractId, organizationId,
                                                  taxiClass, delete);
    }
}