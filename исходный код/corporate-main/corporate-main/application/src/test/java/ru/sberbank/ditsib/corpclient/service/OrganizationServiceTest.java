package ru.sberbank.ditsib.corpclient.service;

import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.corpclient.database.model.OrganizationStatus.INACTIVE;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория организаций")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrganizationServiceTest {

    @Autowired
    private OrganizationService organizationService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EntityManager manager;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Сохранение новой организации")
    void test_saveNewOrganization() {
        var organization = new Organization();
        organization.setAddress("Address");
        organization.setOfficialName("Official name 2");
        organization.setMsrn("Msrn 2");
        organization.setTid("Tin 2");
        organization.setOrganizationCode(555);

        var actual = organizationService.add(organization);

        assertThat(manager.createQuery("SELECT organization FROM Organization organization", Organization.class).getResultList())
                .matches(l -> l.size() == 1);

        assertThat(actual).matches(a -> a.getAddress().equals(organization.getAddress()))
                .matches(a -> a.getOfficialName().equals(organization.getOfficialName()))
                .matches(a -> a.getOrganizationCode().equals(organization.getOrganizationCode()));
    }

    @Test
    void test_deleteOrganization() {
        var organization = new Organization();
        organization.setOfficialName("name");
        organization.setAddress("address");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setOrganizationCode(555);
        manager.persist(organization);

        assertThat(manager.createQuery("SELECT organization FROM Organization organization", Organization.class).getResultList())
                .matches(l -> l.size() == 1);

        organizationService.delete(organization.getId());

        final var result = manager.createQuery("SELECT organization FROM Organization organization", Organization.class).getResultList();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(INACTIVE);
    }

    @Test
    void test_updateOrganization() {
        var organization = new Organization();
        organization.setOfficialName("name");
        organization.setAddress("address");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setOrganizationCode(555);
        manager.persist(organization);

        var newData = new Organization();
        newData.setId(organization.getId());
        newData.setOfficialName("Edited name");
        newData.setAddress("Edited address");
        newData.setMsrn("msrn");
        newData.setTid("tid");
        newData.setOrganizationCode(111);

        assertThat(manager.createQuery("SELECT organization FROM Organization organization", Organization.class).getResultList())
                .hasSize(1);

        organizationService.edit(organization.getId(), newData);

        var list = manager.createQuery("SELECT organization FROM Organization organization", Organization.class)
                .getResultList();

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getAddress()).isEqualTo(newData.getAddress());
        assertThat(list.getFirst().getOfficialName()).isEqualTo(newData.getOfficialName());
        assertThat(list.getFirst().getOrganizationCode()).isEqualTo(newData.getOrganizationCode());
    }
}
