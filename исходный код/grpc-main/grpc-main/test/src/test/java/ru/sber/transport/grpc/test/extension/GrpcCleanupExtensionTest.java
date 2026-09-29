package ru.sber.transport.grpc.test.extension;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.test.ManagedChannelCleanupTarget;
import ru.sber.transport.grpc.test.ServerCleanupTarget;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_test")
@DisplayName("Проверка расширения")
class GrpcCleanupExtensionTest {

    @DisplayName("Проверка")
    @Test
    void test_extension() throws IOException {
        var extension = new GrpcCleanupExtension();
        extension.addService(new TestService("testService"));

        assertThat(extension.getCleanupTargets()).hasSize(2);
        assertThat(extension.getCleanupTargets().get(0)).isInstanceOf(ServerCleanupTarget.class);
        assertThat(extension.getCleanupTargets().get(1)).isInstanceOf(ManagedChannelCleanupTarget.class);

        extension.afterEach(null);
        assertThat(extension.getCleanupTargets()).isEmpty();
    }

    private record TestService(String serviceName) implements BindableService {

        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(serviceName).build();
        }
    }
}