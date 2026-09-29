package ru.sber.transport.corporate_sync.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.corporate_sync.Sync;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messages.easup.avro.*;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Configuration
class ListenerConfig {

    private final Set<String> skip = new HashSet<>();

    @Bean
    Consumer<Message<OrganizationData>> organizationsInputAvro(Sync<DepartmentData> departmentDataSync, Sync<PositionData> positionDataSync, Sync<EmployeeData> employeeDataSync, Sync<DepartmentHeadData> departmentHeadDataSync, Sync<DepartmentParentData> departmentParentDataSync, Sync<EndData> endDataSync) {
        log.info("Organization listener initialized");
        return raw -> {
            var payload = raw.getPayload();
            var data = payload.getData();
            if (skip.contains(payload.getId()) && !(data instanceof EndData)) {
                return;
            }
            try {
                switch (data) {
                    case DepartmentData item -> departmentDataSync.sync(item.getOrganizationId(), item.getId(), item);
                    case PositionData item -> positionDataSync.sync(item.getOrganizationId(), item.getId(), item);
                    case EmployeeData item -> employeeDataSync.sync(item.getOrganizationId(), item.getPersonnelNumber(), item);
                    case DepartmentParentData item -> departmentParentDataSync.sync(item.getOrganizationId(), item.getId(), item);
                    case DepartmentHeadData item -> departmentHeadDataSync.sync(item.getOrganizationId(), item.getId(), item);
                    case EndData item -> {
                        endDataSync.sync(payload.getId(), payload.getId(), item);
                        skip.remove(payload.getId());
                    }
                    default -> throw new IllegalArgumentException("Unknown data type: %s".formatted(data.getClass().getName()));
                }
            } catch (EntityNotFoundException e) {
                log.warn("{} {} is not defined. Skip", e.getEntityName(), e.getEntityId());
                skip.add(ReflectionUtils.castObjectToMap(e.getEntityId(), String.class, String.class).get("syncId"));
            }
        };
    }

}