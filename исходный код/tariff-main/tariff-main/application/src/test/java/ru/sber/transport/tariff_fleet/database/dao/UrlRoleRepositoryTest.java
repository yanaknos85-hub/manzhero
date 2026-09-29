package ru.sber.transport.tariff_fleet.database.dao;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.roles.check.data.dao.UrlRoleRepository;
import ru.sber.transport.tariff_fleet.constant.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static ru.sber.transport.tariff_fleet.constant.Role.*;

@SpringBootTest
@EmbeddedPostgres
@Transactional
class UrlRoleRepositoryTest {
    @Autowired
    private UrlRoleRepository repository;
    
    private static final Set<String> checkedUrlMethods = new HashSet<>();
    private static final Set<String> allUrlMethods = new HashSet<>();
    
    @MethodSource
    @ParameterizedTest(name = "URL:{0} {1}, роли:{2}")
    @DisplayName("Проверка ролей и URL в базе данных")
    void checkRolesAndUrls(HttpMethod method, String testedUrl, List<Role> expectedRoles) {
        addAllRoles();
        var urlMethod = repository.findByMethodAndRestrictedUrl(method, testedUrl).orElseThrow(
                () -> new JUnitException("Такой комбинации %s %s не существует!".formatted(method, testedUrl)));
        assertThat(expectedRoles.stream()
                                .map(Enum::name)
                                .collect(Collectors.toSet())
                  ).containsExactlyInAnyOrderElementsOf(urlMethod.getRoles());
        checkedUrlMethods.add(urlMethod.getMethod() + " " + urlMethod.getRestrictedUrl());
    }
    
    @AfterAll
    static void tearDown() {
        assertThat(checkedUrlMethods).containsExactlyInAnyOrderElementsOf(allUrlMethods);
    }
    
    private void addAllRoles() {
        var urls = new HashSet<String>();
        Arrays.stream(Role.values())
              .map(Enum::name)
              .map(role -> repository.findAllByRole(role)
                                     .stream()
                                     .map(url -> url.getMethod().name() + " " + url.getRestrictedUrl())
                                     .toList()
                  )
              .forEach(urls::addAll);
        allUrlMethods.addAll(urls);
    }
    
    static Stream<Arguments> checkRolesAndUrls() {
        return Stream.of(
                Arguments.of(GET, "/edf-operators/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/fleet-owner-organizations/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/organization/department/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/contracts/{contractId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/contracts/{contractId}/deactivate/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/contracts/{contractId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/contracts/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/contracts/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/contracts/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/contracts/search/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/contracts/search/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/contracts/search/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/contractors/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/tariffs/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/tariffs/{id}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/tariffs/{id}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/tariffs/{id}/deactivate/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/tariffs/search/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/service-points/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/service-points/file/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/contractors/{documentType}/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/contractors/{documentType}/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/contracts/{contractId}/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/contracts/{contractId}/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/contracts/{contractId}/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/contracts/{contractId}/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/contracts/{contractId}/deactivate/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/contracts/{contractId}/deactivate/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/contracts/{contractId}/service-points/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/contracts/{contractId}/service-points/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/tariffs/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/tariffs/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/tariffs/{id}/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/tariffs/{id}/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/tariffs/{id}/deactivate/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/tariffs/{id}/deactivate/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/tariffs/search/all-organizations/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/tariffs/search/self-organization/",
                        List.of(ROLE_ADMIN_CORP_CLIENT))
        );


    }
}