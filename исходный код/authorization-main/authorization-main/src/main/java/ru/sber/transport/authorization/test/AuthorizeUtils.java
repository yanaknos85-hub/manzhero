package ru.sber.transport.authorization.test;

import lombok.experimental.UtilityClass;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Утилита для проверки авторизации. Требует наличия в контексте мока @{link AuthorizationManager}
 *
 * @see AuthorizationManager
 */
@UtilityClass
public class AuthorizeUtils {

    /**
     * Авторизовать запросы с указанной ролью по-умолчанию (USER).
     *
     * @param accessManager мок менеджера доступа.
     */
    public void authorize(AuthorizationManager<?> accessManager) {
        when(accessManager.check(any(), any())).thenAnswer(inv -> mockDecision(inv, "ROLE_USER"));
    }

    /**
     * Авторизовать запросы с указанной ролью.
     *
     * @param accessManager мок менеджера доступа.
     * @param role роль для разрешения доступа.
     */
    public void authorize(AuthorizationManager<?> accessManager, String role) {
        when(accessManager.authorize(any(), any())).thenAnswer(inv -> mockDecision(inv, role));
    }

    private AuthorizationDecision mockDecision(InvocationOnMock inv, String role) {
        var rawAuth = inv.getArgument(0, Supplier.class).get();
        if (rawAuth instanceof Authentication auth) {
            return new AuthorizationDecision(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals));
        }
        return new AuthorizationDecision(false);
    }
}
