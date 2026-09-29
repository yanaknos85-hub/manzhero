package ru.sber.transport.limits.web.http.impl;

import io.qameta.allure.Feature;
import lombok.NonNull;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.limits.enums.SharingType;
import ru.sber.transport.dto.Page;
import ru.sber.transport.limits.business.Limits;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.ModifiedLimit;
import ru.sber.transport.limits.web.api.ManagingApiDelegate;
import ru.sber.transport.limits.web.http.mappers.*;
import ru.sber.transport.limits.web.http.model.LimitWebFilter;
import ru.sber.transport.limits.web.model.*;
import ru.sber.transport.limits.web.providers.DepartmentsProvider;
import ru.sber.transport.limits.web.providers.EmployeeProvider;
import ru.sber.transport.limits.web.providers.SharingsProvider;
import ru.sberbank.ditsib.request.Direction;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка делегата управления лимитами")
class ManagingDelegateTest {

    private final Limits limits = mock(Limits.class);

    private final LimitWebMapperImpl mapper = new LimitWebMapperImpl(new StatusWebMapperImpl(), new SharingTypeWebMapperImpl(), new SumWebMapperImpl());

    private final EmployeeProvider employees = mock(EmployeeProvider.class);

    private final DepartmentsProvider departments = mock(DepartmentsProvider.class);

    private final SharingsProvider sharings = mock(SharingsProvider.class);

    private final EmployeeWebMapper employeeMapper = new EmployeeWebMapperImpl();

    private final DepartmentWebMapper departmentMapper = new DepartmentWebMapperImpl();

    private final SharingsWebMapper sharingsMapper = new SharingsWebMapperImpl(new SumWebMapperImpl());

    private final SpelExpressionParser parser = new SpelExpressionParser(new SpelParserConfiguration(false, true));

    private final SortMapperImpl sortMapper = new SortMapperImpl();

    private final PageMapperImpl pageMapper = new PageMapperImpl();

    private final ManagingApiDelegate delegate = new ManagingDelegate(mapper, limits, parser, sortMapper, pageMapper, employees, departments, sharings, employeeMapper, departmentMapper, sharingsMapper);

    @Test
    @DisplayName("Проверка удаления лимита")
    void test_delete() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();
        var response = delegate.delete(limitId);

        verify(limits).delete(limitId, userId, false);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Получение данных лимита")
    void test_get() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();

        var limit = Instancio.create(Limit.class);

        when(limits.get(userId, false, limitId)).thenReturn(limit);

        var response = delegate.get(limitId, Optional.empty());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.getHash()));
    }

    @Test
    @DisplayName("Получение данных лимита. Установлен фильтр модификации")
    void test_get_modified() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();

        var limit = Instancio.create(ModifiedLimit.class);

        var modified = OffsetDateTime.now();

        when(limits.get(userId, false, limitId, modified)).thenReturn(limit);

        var response = delegate.get(limitId, Optional.of(modified));

        assertThat(response.getStatusCode()).isEqualTo(limit.modified() ? HttpStatus.OK : HttpStatus.NOT_MODIFIED);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.data().getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.data().getHash()));
    }

    @Test
    @DisplayName("Получение хэша лимита")
    void test_hash() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();

        var limit = Instancio.create(Limit.class);

        when(limits.hash(userId, false, limitId)).thenReturn(limit);

        var response = delegate.head(limitId, Optional.empty());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.getHash()));
    }

    @Test
    @DisplayName("Получение хэша лимита. Установлен фильтр модификации")
    void test_hash_modified() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();

        var limit = Instancio.create(ModifiedLimit.class);

        var modified = OffsetDateTime.now();

        when(limits.hash(userId, false, limitId, modified)).thenReturn(limit);

        var response = delegate.head(limitId, Optional.of(modified));

        assertThat(response.getStatusCode()).isEqualTo(limit.modified() ? HttpStatus.NO_CONTENT : HttpStatus.NOT_MODIFIED);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.data().getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.data().getHash()));
    }

    @Test
    @DisplayName("Проверка изменения данных")
    void test_put() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();
        var newData = Instancio.create(NewLimit.class);
        var limit = Instancio.create(Limit.class);
        newData.setStatus(Status.SHARED);
        newData.setServiceType("CARGO");
        newData.setSharingType(SharedType.MONTHLY);
        limit.setStatus(ru.sber.transport.limits.business.model.Status.PLANNING);
        limit.setServiceType("PASSENGER");

        when(limits.update(eq(limitId), eq(userId), eq(false), any(), anyList())).thenReturn(limit);

        var response = delegate.put(newData, limitId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.getHash()));

        var fields = ArgumentCaptor.forClass(List.class);
        var newDataCaptor = ArgumentCaptor.forClass(Limit.class);

        verify(limits).update(eq(limitId), eq(userId), eq(false), newDataCaptor.capture(), fields.capture());

        var list = fields.getValue();
        assertThat(list).hasSameElementsAs(Arrays.stream(newData.getClass().getDeclaredFields()).map(Field::getName).map(it -> "REPLACE:" + it).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("Проверка частичного изменения данных")
    void test_patch() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .jti(userId.toString())
                .build()));

        var limitId = UUID.randomUUID();
        var newData = new ArrayList<PatchRequestInner>();

        var status = new PatchRequestInner(PatchRequestInner.OpEnum.ADD, "/status");
        var year = new PatchRequestInner(PatchRequestInner.OpEnum.REPLACE, "/year");
        var sharingType = new PatchRequestInner(PatchRequestInner.OpEnum.REMOVE, "/sharingType");
        var responsibles = new PatchRequestInner(PatchRequestInner.OpEnum.ADD, "/responsibles[3]");

        status.setValue(Status.CLOSED);
        year.setValue(Instancio.create(Integer.class));
        sharingType.setValue(Instancio.create(SharingType.class).name());
        responsibles.setValue(Instancio.create(UUID.class));

        newData.add(status);
        newData.add(year);
        newData.add(sharingType);
        newData.add(responsibles);

        var limit = Instancio.create(Limit.class);

        when(limits.update(eq(limitId), eq(userId), eq(false), any(), anyList())).thenReturn(limit);

        var response = delegate.patch(newData, limitId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(getHeaders(response, HttpHeaders.LAST_MODIFIED).getFirst()).isEqualTo(limit.getUpdateTime().format(DateTimeFormatter.ofPattern("EE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)));
        assertThat(getHeaders(response, HttpHeaders.ETAG).getFirst()).isEqualTo("\"%s\"".formatted(limit.getHash()));

        var fieldsCaptor = ArgumentCaptor.forClass(List.class);
        var newDataCaptor = ArgumentCaptor.forClass(Limit.class);

        verify(limits).update(eq(limitId), eq(userId), eq(false), newDataCaptor.capture(), fieldsCaptor.capture());

        assertThat(fieldsCaptor.getValue()).hasSameElementsAs(List.of("ADD:status", "REPLACE:year", "ADD:responsibles[3]", "REMOVE:sharingType"));
        var item = newDataCaptor.getValue();
        assertThat(item.getStatus().name()).isEqualTo(status.getValue().toString());
        assertThat(item.getYear()).isEqualTo(year.getValue());
        assertThat(item.getResponsibles()).hasSize(3);
        assertThat(item.getResponsibles().get(0)).isNull();
        assertThat(item.getResponsibles().get(1)).isNull();
        assertThat(item.getResponsibles().get(2)).isEqualTo(responsibles.getValue());
    }

    @Test
    @DisplayName("Проверка получения списка")
    void test_getAll() {
        var departmentId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var status = Status.SHARED;
        var serviceType = Instancio.create(String.class);
        var page = Instancio.create(Integer.class);
        var size = Instancio.create(Integer.class);
        var sort = Instancio.create(String.class);
        var direction = Instancio.create(SortDirection.class);

        var pageData = new Page<>(new PageImpl<>(Instancio.createList(Limit.class), Pageable.ofSize(size), size * 10));

        when(limits.get(LimitWebFilter.builder().departmentId(departmentId).year(year).status(ru.sber.transport.limits.business.model.Status.valueOf(status.name())).serviceType(serviceType).build(), page, size, sort, Direction.valueOf(direction.name())))
                .thenReturn(pageData);

        var result = delegate.getAll(
                Optional.empty(),
                Optional.of(departmentId),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(year),
                Optional.of(status),
                Optional.empty(),
                Optional.of(serviceType),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(page),
                Optional.of(size),
                Optional.of(sort),
                Optional.of(direction)
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        var body = result.getBody();
        assertThat(body).isNotNull();

        var content = body.getContent();
        assertThat(content).hasSameSizeAs(pageData.getContent());
    }

    @NonNull
    private static List<String> getHeaders(ResponseEntity<?> response, String headerName) {
        return Optional.ofNullable(response.getHeaders().get(headerName)).orElseGet(List::of);
    }

}