package issues;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.Account;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.sudir.SudirApplication;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@SpringBootTest(classes = SudirApplication.class)
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@Feature("app_platform_oauth")
@DisplayName("E-t-E проверка нагрузки")
@ActiveProfiles("test")
@Slf4j
@Disabled("Включать только при необходимости проверки производительности")
class Transport30515Test {

    @Autowired
    @Qualifier("usersInput")
    private Consumer<Message<UserMessage>> consumer;

    @Autowired
    private DSLContext ctx;

    @Test
    @DisplayName("Отправка списка")
    void test_sendList() {

        var stopwatch = new StopWatch();
        stopwatch.start("Saving list");
        int countToSave = 100000;
        for (int i = 0; i < countToSave; i++) {
            consumer.accept(MessageBuilder.createMessage(Instancio.of(UserMessage.class).create(), new MessageHeaders(Map.of("type", "INTERNAL"))));
        }
        stopwatch.stop();

        var count = ctx.fetchCount(ctx.selectFrom(Account.ACCOUNT));
        assertThat(count).isEqualTo(countToSave);
        var took = Duration.ofMillis(stopwatch.getTotalTimeMillis());
        log.info("{} items saved in {} ({} I/S)", countToSave, took, countToSave / took.getSeconds());
    }
}
