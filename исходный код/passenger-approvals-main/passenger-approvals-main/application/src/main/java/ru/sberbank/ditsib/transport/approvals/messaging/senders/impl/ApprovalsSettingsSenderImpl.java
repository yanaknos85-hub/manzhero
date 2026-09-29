package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.approvals.messaging.ApprovalsSettingsMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalsSettingsMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApprovalsSettingsSender;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalsSettingsSenderImpl implements ApprovalsSettingsSender {

    @Qualifier("approvalsSettingsOutput")
    private final ObjectProvider<OutputBridge> approvalsSettingsOutput;

    private final ApprovalsSettingsMapper mapper;
    
    @Override
    public void send(ApprovalsSettings settings) {
        ApprovalsSettingsMessage message = switch (settings.getTransportType()) {
            case "TAXI" -> mapper.toTaxiMessage((TaxiApprovalsSettings) settings);
            case "PUBLIC" -> mapper.toPublicMessage((PublicTrApprovalsSettings) settings);
            case "GROUP_TRANSFER" -> mapper.toGroupTransferMessage((GroupTransferApprovalsSettings) settings);
            default -> mapper.toOtherTrMessage((OtherTrTypesApprovalsSettings) settings);
        };
        log.debug("Были добавлены / изменены настройки согласований {} для корп.клиента {}",
                  message.getTransportType(), message.getOrganizationId());
        approvalsSettingsOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
    
    @Override
    public void sendDeleted(ApprovalsSettings settings) {
        ApprovalsSettingsMessage message =
                ApprovalsSettingsMessage.builder()
                                        .id(settings.getId())
                                        .transportType(settings.getTransportType())
                                        .deleted(true)
                                        .build();
        log.debug("Были удалены настройки согласований {} для корп.клиента {}",
                  message.getTransportType(), message.getOrganizationId());
        approvalsSettingsOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
