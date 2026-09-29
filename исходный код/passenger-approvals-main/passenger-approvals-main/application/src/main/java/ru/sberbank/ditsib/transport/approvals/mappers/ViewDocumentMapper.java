package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;
import ru.sberbank.ditsib.transport.approvals.messaging.message.ViewDocumentMessage;

/**
 * Маппер просмотров документов.
 */
@Mapper
public interface ViewDocumentMapper {

    @Mapping(target = "id", source = "documentId")
    ViewDocument toModel(ViewDocumentMessage message);
}
