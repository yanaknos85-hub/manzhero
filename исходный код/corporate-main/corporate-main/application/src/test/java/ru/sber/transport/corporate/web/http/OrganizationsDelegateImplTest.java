package ru.sber.transport.corporate.web.http;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.Organizations;
import ru.sber.transport.corporate.web.http.mappers.OrganizationWebMapper;
import ru.sber.transport.corporate.web.http.mappers.OrganizationWebMapperImpl;
import ru.sber.transport.utils.DataCreator;
import ru.sber.transport.web.api.OrganizationsCommandsApi;
import ru.sber.transport.web.model.NewOrganizationData;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка делегата организаций")
class OrganizationsDelegateImplTest implements DataCreator {

    private final OrganizationWebMapper mapper = new OrganizationWebMapperImpl();

    private final Organizations organizations = mock(Organizations.class);

    private final OrganizationsCommandsApi organizationsDelegate = new OrganizationsDelegateImpl(mapper, organizations);

    @Test
    @DisplayName("Добавление организации")
    void test_add() {
        var data = Instancio.of(NewOrganizationData.class)
                .ignore(Select.field(NewOrganizationData::getContacts))
                .create();
        var mapped = createOrganization(data);
        var saved = createOrganization(mapped);

        when(organizations.add(mapped)).thenReturn(saved);

        var response = organizationsDelegate.add(data);

        assertSoftly(it -> {
            it.assertThat(response).isNotNull();
            it.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            it.assertThat(response.getBody()).isNotNull();
        });
        assertSoftly(it -> {
            it.assertThat(response).isNotNull();
            it.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            var body = response.getBody();
            it.assertThat(body.getAddress()).isEqualTo(saved.getAddress());
            it.assertThat(body.getOfficialName()).isEqualTo(saved.getName());
            it.assertThat(body.getMsrn()).isEqualTo(saved.getMsrn());
            it.assertThat(body.getOrganizationCode()).isEqualTo(saved.getCode());
            it.assertThat(body.getTid()).isEqualTo(saved.getTid());
            it.assertThat(body.getDigitId()).isEqualTo(saved.getDigitId());
            it.assertThat(body.getEasupId()).isEqualTo(saved.getSyncId());
            it.assertThat(body.getContacts()).hasSameSizeAs(saved.getContacts());
            it.assertThat(body.getId()).isEqualTo(saved.getId());
            it.assertThat(body.getStatus().name()).isEqualTo(saved.getStatus().name());
        });
    }

}