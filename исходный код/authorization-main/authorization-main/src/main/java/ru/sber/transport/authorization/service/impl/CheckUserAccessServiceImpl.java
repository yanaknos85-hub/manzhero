package ru.sber.transport.authorization.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса проверки полномочий пользователя.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class CheckUserAccessServiceImpl implements CheckUserAccessService {
    
    private final List<EmployeeOrganizationFunction> organizationFunctions;
    
    @Override
    public void check() throws UnauthorizedException {
        if (isNotDataMaster()) {
            throw new UnauthorizedException(getAuthenticatedUser());
        }
    }
    
    @Override
    public void check(@NonNull UUID organizationId) throws UnauthorizedException {
        var organizationFunction = getFunction();
        if (organizationFunction == null) {
            throw new UnauthorizedException("Checking organization access is not supported. Please define " +
                                                    "`Function<UUID, UUID>` which provides data for checking");
        }
        var authenticated = getAuthenticatedUser();
        var organization = organizationFunction.apply(authenticated);
        if (isNotDataMaster()) {
            if (organization == null) {
                log.error("Organization not found");
                throw new UnauthorizedException(authenticated);
            }
            if (!organization.equals(organizationId)) {
                log.info("Requested organization is {}, acceess to organization {}", organizationId, organization);
                throw new UnauthorizedException(authenticated);
            }
        }
    }
    
    /**
     * Поиск функции, обеспечивающей получение идентификатора организации пользователя. Если функций больше одной,
     * будет взята первая попавшаяся.
     *
     * @return функция получения идентификатора организации.
     */
    private EmployeeOrganizationFunction getFunction() {
        if (organizationFunctions.isEmpty()) {
            log.debug("There is no organization function implementations found. Checking for organization access will" +
                      " not available");
            return null;
        }
        if (organizationFunctions.size() > 1) {
            log.warn("Found more then 1 implementation of organization function. The first one will be used.");
        }
        return organizationFunctions.get(0);
    }
    
    /**
     * Проверка текущего пользователя, является ли он обычным пользователем.
     *
     * @return <code>true</code> если нет.
     */
    private boolean isNotDataMaster() {
        var authentication = getAuthentication();
        var principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return !Boolean.TRUE.equals(jwt.getClaimAsBoolean("data_master"));
        }
        return true;
    }
    
    /**
     * Получение данных аутентифицированного пользователя.
     *
     * @return идентификатор пользователя.
     */
    private UUID getAuthenticatedUser() {
        var authentication = getAuthentication();
        return Optional.ofNullable(authentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getId)
                .map(UUID::fromString).orElse(null);
    }
    
    /**
     * Получение данных аутентификации.
     *
     * @return данные аутентификации.
     */
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
