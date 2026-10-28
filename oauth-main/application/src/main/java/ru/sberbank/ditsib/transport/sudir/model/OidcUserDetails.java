package ru.sberbank.ditsib.transport.sudir.model;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.util.Map;

/**
 * User details from OIDC.
 */
public class OidcUserDetails extends AccountUserDetails implements OidcUser {

    private final OidcUser wrappedOidcUser; // NOSONAR

    /**
     * Create a new details object.
     *
     * @param account account to define.
     * @param wrappedOidcUser wrapped user.
     */
    public OidcUserDetails(Account account, OidcUser wrappedOidcUser) {
        super(account);
        this.wrappedOidcUser = wrappedOidcUser;
    }

    @Override
    public Map<String, Object> getClaims() {
        return wrappedOidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return wrappedOidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return wrappedOidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return wrappedOidcUser.getAttributes();
    }

    @Override
    public String getName() {
        return wrappedOidcUser.getName();
    }
}
