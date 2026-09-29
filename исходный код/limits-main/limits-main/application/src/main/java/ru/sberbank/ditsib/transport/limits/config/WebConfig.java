package ru.sberbank.ditsib.transport.limits.config;

import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.v2.GeneralAnalyticalReportRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
class WebConfig implements WebMvcConfigurer {


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NonNull MethodParameter parameter) {
                return parameter.getParameterType().getCanonicalName().equals(GeneralAnalyticalReportRequestV2DTO.class.getCanonicalName());
            }

            @Override
            public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                var parameters = webRequest.getParameterMap();
                return new GeneralAnalyticalReportRequestV2DTO(
                    Arrays.stream(Optional.ofNullable(parameters.get("organizationId[]")).orElse(new String[0])).parallel().map(String::trim).map(UUID::fromString).toList(),
                    Arrays.stream(Optional.ofNullable(parameters.get("year")).orElse(new String[0])).parallel().map(String::trim).map(Integer::parseInt).findFirst().orElse(null),
                    Arrays.stream(Optional.ofNullable(parameters.get("month[]")).orElse(new String[0])).parallel().map(String::trim).map(Month::valueOf).toList(),
                    Arrays.stream(Optional.ofNullable(parameters.get("transportType[]")).orElse(new String[0])).parallel().map(String::trim).map(TransportTypeEnum::valueOf).toList()
                );
            }
        });
    }
}
