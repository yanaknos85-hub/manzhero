package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;

@Mapper
public interface DeadlineSettingsMapper {
    
    @Mapping(target = "employeeLimitChronoUnit", source = "employeeLimitDeadline.unit")
    @Mapping(target = "employeeLimitValue", source = "employeeLimitDeadline.value")
    @Mapping(target = "departmentLimitChronoUnit", source = "departmentLimitDeadline.unit")
    @Mapping(target = "departmentLimitValue", source = "departmentLimitDeadline.value")
    DeadlineSettings fromMessage(DeadlineSettingsMessage message);
}
