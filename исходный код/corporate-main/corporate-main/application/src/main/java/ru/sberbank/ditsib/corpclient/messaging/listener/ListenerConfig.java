package ru.sberbank.ditsib.corpclient.messaging.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.service.*;
import ru.sberbank.ditsib.transport.messaging.messages.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Конфигурация слушателей.
 */
@Slf4j
@Configuration("oldListenerConfig")
class ListenerConfig {

    @Bean
    Consumer<Message<GeoZoneMessage>> geoZoneInput(GeoZoneService geoZoneService) {
        return message -> {
            var payload = message.getPayload();
            var geoZone = geoZoneService.get(payload.getId()).orElseGet(GeoZone::new);

            if (payload.isDeleted()) {
                geoZoneService.delete(geoZone);
            } else {
                geoZone.setId(payload.getId());
                geoZone.setCode(Optional.ofNullable(payload.getCode()).map(String::valueOf).orElse(null));
                geoZone.setName(payload.getName());
                geoZone.setParentId(payload.getParentId());

                geoZoneService.save(geoZone);
            }
        };
    }

    @Bean
    Consumer<Message<Map<String, Object>>> requestInput(TripPurposeStatisticService tripPurposeStatisticService) {
        return message -> {
            var payload = message.getPayload();
            try {
                var authorId = Optional.ofNullable(payload.get("authorId")).map(String::valueOf).map(UUID::fromString).orElse(null);
                var purposeId = Optional.ofNullable(payload.get("purposeId")).map(String::valueOf).map(UUID::fromString).orElse(null);
                if (authorId == null) {
                    log.warn("Author ID is null: {}", payload.get("id"));
                    log.debug("... Data: {}", payload);
                    return;
                }
                if (purposeId == null) {
                    log.warn("Purpose ID is null: {}", payload.get("id"));
                    log.debug("... Data: {}", payload);
                    return;
                }
                tripPurposeStatisticService.addTripPurposeToStatistic(authorId, purposeId);
            } catch (Exception e) {
                log.warn("Unable save request message: {}", e.getMessage(), e);
            }
        };
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInput(RolesService rolesService) {
        return message -> {
            var code = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, String.class);
            var payload = message.getPayload();
            code = Optional.ofNullable(code).orElse(payload.code());
            if (Boolean.TRUE.equals(payload.deleted())) {
                rolesService.delete(code);
            } else {
                var role = Optional.ofNullable(code).map(rolesService::get)
                        .flatMap(Function.identity())
                        .orElseGet(Role::new);

                role.setCode(payload.code());
                role.setName(payload.name());

                rolesService.save(role);
            }
        };
    }

    @Bean
    Consumer<Message<Map<String, Object>>> tripRequestApproveInput(EmployeeService employeeService) {
        return message -> {
            var payload = message.getPayload();
            try {
                var actionId = Optional.ofNullable(payload.get("actionId")).map(String::valueOf).map(UUID::fromString).orElse(null);
                var actorEmployeeId = Optional.ofNullable(payload.get("actorEmployeeId")).map(String::valueOf).map(UUID::fromString).orElse(null);

                if (actorEmployeeId == null) {
                    return;
                }
                if (payload.containsKey("approved") && Boolean.TRUE.equals(payload.get("approved"))) {
                    employeeService.addApproval(actionId, actorEmployeeId);
                } else {
                    employeeService.deleteApproval(actionId, actorEmployeeId);
                }
            } catch (ClassCastException e) {
                log.warn("Handling message of trip request approving trouble. {}", e.getMessage());
            }
        };
    }

    @Bean
    Consumer<Message<AccountRoleLinkMessage>> accountsRolesInput(EmployeeService employeeService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, String.class);

            if (id != null && Pattern.matches("[a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}", id)) {
                employeeService.update(UUID.fromString(id), message);
            } else {
                log.warn("Received id is not valid uuid: %s".formatted(id));
            }
        };
    }

    @Bean
    Consumer<Message<UserDataConfirmationMessage>> confirmationDataInput(ContactConfirmationService contactConfirmationService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if(message.getId() != null && message.getPhone() != null) {
                contactConfirmationService.confirm(message);
            } else {
                log.warn("Received data is invalid");
            }
        };
    }

    @Bean
    Consumer<Message<UserDataConfirmationMessage>> confirmationDataInputSsl(ContactConfirmationService contactConfirmationService) {
        return confirmationDataInput(contactConfirmationService);
    }

    @Bean
    Consumer<Message<GeoZoneMessage>> geoZoneInputSsl(GeoZoneService geoZoneService) {
        return geoZoneInput(geoZoneService);
    }

    @Bean
    Consumer<Message<Map<String, Object>>> requestInputSsl(TripPurposeStatisticService tripPurposeStatisticService) {
        return requestInput(tripPurposeStatisticService);
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInputSsl(RolesService rolesService) {
        return rolesInput(rolesService);
    }

    @Bean
    Consumer<Message<Map<String, Object>>> tripRequestApproveInputSsl(EmployeeService employeeService) {
        return tripRequestApproveInput(employeeService);
    }

    @Bean
    Consumer<Message<AccountRoleLinkMessage>> accountsRolesInputSsl(EmployeeService employeeService) {
        return accountsRolesInput(employeeService);
    }

}
