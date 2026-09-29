package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TripPurposeMessage;

/**
 * Маппер целей
 */
@Mapper
public interface TripPurposeMapper {
    TripPurpose toModel(TripPurposeMessage message);
}
