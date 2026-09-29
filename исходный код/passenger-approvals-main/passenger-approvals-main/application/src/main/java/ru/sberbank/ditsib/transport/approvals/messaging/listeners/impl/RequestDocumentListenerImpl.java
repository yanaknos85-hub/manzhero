package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.mappers.RequestDocumentMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.RequestDocumentListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestDocumentMessage;
import ru.sberbank.ditsib.transport.approvals.services.RequestDocumentService;

/**
 * Реализация слушателя создания/удаления документа.
 */
@RequiredArgsConstructor
@Slf4j
public class RequestDocumentListenerImpl implements RequestDocumentListener {

    private final RequestDocumentMapper mapper;

    private final RequestDocumentService service;

    @Override
    public void handle(RequestDocumentMessage message) {
        if (message.isDeleted()) {
            service.delete(message.getDocumentId());
        } else {
            RequestDocument document = mapper.toModel(message);
            service.save(document);
        }
    }
}
