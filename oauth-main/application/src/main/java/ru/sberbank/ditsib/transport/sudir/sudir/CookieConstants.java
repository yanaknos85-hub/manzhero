package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.experimental.UtilityClass;

/**
 * Constats of cookies.
 */
@UtilityClass
public class CookieConstants {

    /**
     * Original referer (for final redirection) but not reserved token.
     */
    public static final String ORIGINAL_REFERER_ALTERNATIVE = "Original_referer_alternative";

    /**
     * Authorization session id.
     */
    public static final String AUTHORIZATION_SESSION_ID = "Authorization_session_id";
}
