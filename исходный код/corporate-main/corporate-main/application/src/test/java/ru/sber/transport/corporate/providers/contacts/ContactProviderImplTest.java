package ru.sber.transport.corporate.providers.contacts;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.providers.ContactProvider;
import ru.sber.transport.corporate.providers.contacts.mappers.ContactDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.corporate.Tables.CONTACT;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера контактов")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, ContactProviderImpl.class, ContactDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class ContactProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private ContactProvider provider;

    @DisplayName("Сохранение контакта")
    @Test
    void test_save() {
        var contact = Instancio.create(Contact.class);

        var saved = provider.save(contact);

        assertThat(context.fetchCount(context.selectFrom(CONTACT))).isEqualTo(1);

        var actual = context.selectFrom(CONTACT).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(saved.getId());
            it.assertThat(actual.getType().name()).isEqualTo(saved.getType().name()).isEqualTo(contact.getType().name());
            it.assertThat(actual.getValue()).isEqualTo(saved.getValue()).isEqualTo(contact.getValue());
        });
    }

}