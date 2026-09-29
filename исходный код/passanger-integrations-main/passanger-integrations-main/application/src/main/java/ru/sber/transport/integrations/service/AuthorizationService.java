package ru.sber.transport.integrations.service;

import ru.sber.transport.integrations.dto.CredentialClient;

public interface AuthorizationService {

    /**
     * Аутентификация в системе Контрагента
     */
    void auth(CredentialClient auth);

    /**
     * Авторизация в системе Контрагента по refresh токену
     * 
     * @param auth
     */
    void refresh(CredentialClient auth);

}
