package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;
import ru.sberbank.ditsib.transport.approvals.mappers.ViewDocumentMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.ViewDocumentListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.ViewDocumentMessage;
import ru.sberbank.ditsib.transport.approvals.services.RequestDocumentService;

/**
 * Реализация слушателя просмотра документа.
 */
@RequiredArgsConstructor
@Slf4j
public class ViewDocumentListenerImpl implements ViewDocumentListener {

    private final ViewDocumentMapper mapper;

    private final RequestDocumentService service;

    @Override
    public void handle(ViewDocumentMessage message) {
        ViewDocument viewDocument = mapper.toModel(message);
        service.documentViewedBy(viewDocument);
    }

}
