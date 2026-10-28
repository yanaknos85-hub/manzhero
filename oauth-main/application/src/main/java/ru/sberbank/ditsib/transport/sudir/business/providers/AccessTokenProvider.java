package ru.sberbank.ditsib.transport.sudir.business.providers;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.security.spec.InvalidKeySpecException;

/**
 * Провайдер токенов доступа.
 */
public interface AccessTokenProvider {

    /**
     * Раздел токена с ролями.
     */
    String ROLES_CLAIM_NAME = "roles";

    /**
     * Раздел токена с признаком транспортного пароля.
     */
    String DATAMASTER_CLAIM_NAME = "data_master";

    /**
     * Идентификатор запроса, выдавшего токен.
     */
    String REQUEST_ID_CLAIM_NAME = "requestId";

    /**
     * Случайное число, обеспечивающее уникальность.
     */
    String RANDOM_CLAIM_NAME = "random";

    /**
     * Область видимости пользователя.
     */
    String SCOPE_CLAIM_NAME = "scope";
    
    /**
     * Сгенерировать токен.
     *
     * @param account УЗ для генерации токена.
     * @return токен.
     * @throws InvalidKeySpecException неверная спека формирования ключа.
     */
    String generate(Account account) throws InvalidKeySpecException;
    
    /**
     * Отправить токен в черный список.
     *
     * @param token токен.
     */
    void toBlackList(String token);
}
