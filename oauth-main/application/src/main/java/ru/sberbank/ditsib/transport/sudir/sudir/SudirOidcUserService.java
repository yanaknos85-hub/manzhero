package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.model.OidcUserDetails;

@Slf4j
@Component
public class SudirOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("DEBUGSUDIR point 3");
        var oidcUser = super.loadUser(userRequest);
        var employeeNumber = oidcUser.getClaimAsString("employeenumber");
        log.debug("DEBUGSUDIR point 3.1: employeeNumber = " + employeeNumber);
        var account = Account.builder()
                                .personnelNumber(employeeNumber)
                                .build();
        return new OidcUserDetails(account, oidcUser);
    }
}


