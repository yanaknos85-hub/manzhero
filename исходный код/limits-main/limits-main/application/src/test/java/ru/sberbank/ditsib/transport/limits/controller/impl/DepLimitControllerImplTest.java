package ru.sberbank.ditsib.transport.limits.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.web.api.ManagingApiDelegate;
import ru.sber.transport.limits.web.model.PatchRequestInner;
import ru.sberbank.ditsib.transport.limits.controller.DepLimitController;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitEditDTO;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка работы импорта-экспорта")
class DepLimitControllerImplTest {

    private final ManagingApiDelegate managingApiDelegate = mock(ManagingApiDelegate.class);

    private final DepLimitController controller = new DepLimitControllerImpl(null, null, null, null, null, null, null, null, null, managingApiDelegate);

    @Test
    @DisplayName("Проверка изменения статуса и флагов")
    void test_editStatusOtFlags() throws JsonProcessingException {
        final var limitId = UUID.randomUUID();
        final var dto = Instancio.create(DepLimitEditDTO.class);

        controller.editStatusOrFlags(limitId, dto, null, null);

        final var patch = ArgumentCaptor.forClass(List.class);

        verify(managingApiDelegate).patch(patch.capture(), eq(limitId));

        final var patches = (List<PatchRequestInner>) patch.getValue();

        assertThat(patches).isNotEmpty();
        assertThat(patches.parallelStream().map(PatchRequestInner::getOp).toList()).allMatch(PatchRequestInner.OpEnum.REPLACE::equals);
        assertThat(patches.parallelStream().filter(it -> "/status".equals(it.getPath())).toList()).allMatch(it -> dto.getLimitStatus().name().equals(it.getValue()));
    }
}