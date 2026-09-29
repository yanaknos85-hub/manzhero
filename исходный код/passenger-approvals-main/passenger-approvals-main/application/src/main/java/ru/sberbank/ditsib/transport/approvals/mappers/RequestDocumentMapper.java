package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestDocumentMessage;

/**
 * Маппер документов заявки.
 */
@Mapper
public interface RequestDocumentMapper {
    RequestDocument toModel(RequestDocumentMessage message);
}
