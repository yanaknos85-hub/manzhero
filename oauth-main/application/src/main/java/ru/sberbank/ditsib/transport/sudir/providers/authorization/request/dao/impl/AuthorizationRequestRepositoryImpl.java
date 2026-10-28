package ru.sberbank.ditsib.transport.sudir.providers.authorization.request.dao.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.database.sudir.Tables;
import ru.sber.transport.database.sudir.tables.AuthorizationRequest;
import ru.sberbank.ditsib.transport.sudir.providers.authorization.request.dao.AuthorizationRequestRepository;

@Component
public class AuthorizationRequestRepositoryImpl implements AuthorizationRequestRepository {

    @Override
    public AuthorizationRequest table() {
        return Tables.AUTHORIZATION_REQUEST;
    }
}
