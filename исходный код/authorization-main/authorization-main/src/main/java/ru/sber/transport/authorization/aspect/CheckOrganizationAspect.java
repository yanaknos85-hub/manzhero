package ru.sber.transport.authorization.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.authorization.service.CheckUserAccessService;

import java.util.UUID;

/**
 * Перехват вызова для проверки полномочий вошедшего ползователя относительно запрашиваемой организации.
 */
@Slf4j
@Aspect
@Component
public class CheckOrganizationAspect {

    private final ObjectProvider<CheckUserAccessService> serviceProvider;

    public CheckOrganizationAspect(ObjectProvider<CheckUserAccessService> serviceProvider) {
        this.serviceProvider = serviceProvider;
        if (serviceProvider.getIfAvailable() == null) {
            log.warn("The service for checking organization permissions not configured. It will be ignored");
        }
    }

    /**
     * Проверка прав запроса к данным организации.
     *
     * @param joinPoint данные выполнения в срезе.
     * @return результат выполнения целевого метода.
     * @throws Throwable произошла ошибка при выполнении целевого метода или окружения.
     */
    @Around("@annotation(ru.sber.transport.authorization.annotations.CheckOrganizationAccess)")
    public Object checkOrganization(ProceedingJoinPoint joinPoint) throws Throwable {
        final var service = serviceProvider.getIfAvailable();
        log.debug("Checking organization access");
        var args = joinPoint.getArgs();
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        var annotations = method.getParameterAnnotations();
        UUID organizationId = null;
        log.debug("Looking for an organization parameter");
        for (var parameterIndex = 0; parameterIndex < annotations.length; parameterIndex++) {
            var parameterAnnotations = annotations[parameterIndex];
            for (var annotation : parameterAnnotations) {
                if (annotation.annotationType().isAssignableFrom(Organization.class)) {
                    log.debug("The organization parameter found");
                    var annotationInstance = (Organization) annotation;
                    var valueField = annotationInstance.value();
                    var value = args[parameterIndex];
                    if (valueField.isEmpty()) {
                        log.debug("Parameter is the organization UUID");
                        organizationId = (UUID) value;
                    } else {
                        log.debug("Parameter is a container of the organization UUID");
                        var field = value.getClass().getDeclaredField(valueField);
                        field.setAccessible(true); // NOSONAR
                        organizationId = (UUID) field.get(value);
                    }
                    break;
                }
            }
        }
        if (service != null) {
            if (organizationId != null) {
                service.check(organizationId);
            } else {
                log.debug("The organization parameter not found. Checking data master access only");
                service.check();
            }
        }
        return joinPoint.proceed(args);
    }

}
