package ru.sberbank.ditsib.corpclient.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.controller.TransportOrgController;
import ru.sberbank.ditsib.corpclient.mapper.TransportTypeMapper;
import ru.sberbank.ditsib.corpclient.mapper.TransportTypeMapperImpl;
import ru.sberbank.ditsib.corpclient.messaging.sender.TransportOrgSender;
import ru.sberbank.ditsib.corpclient.service.TransportOrgService;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка контроллера типов транспорта")
class TransportOrgControllerImplTest {

    private final List<String> serviceTypes = List.of("EMPLOYEE_TRANSPORTATION", "CARGO_TRANSPORTATION", "REPAIR", "EXTERNAL");

    private final TransportOrgService service = mock(TransportOrgService.class);

    private final TransportTypeMapper mapper = new TransportTypeMapperImpl();

    private final TransportOrgSender sender = mock(TransportOrgSender.class);

    private final TransportOrgController controller = new TransportOrgControllerImpl(service, mapper, sender);

    public static Stream<Arguments> serviceTypeSource() {
        return Stream.of(
                Arguments.of("EMPLOYEE_TRANSPORTATION"),
                Arguments.of("CARGO_TRANSPORTATION"),
                Arguments.of("REPAIR"),
                Arguments.of("EXTERNAL")
        );
    }

    @MethodSource("serviceTypeSource")
    @ParameterizedTest
    @DisplayName("Получение типов транспорта организации и вида услуги")
    void test_getByOrganization_serviceType_employees(final String serviceType) {
        final var organizationId = Instancio.create(UUID.class);
        final var list = IntStream.range(0, new Random().nextInt(0, 100)).mapToObj(i -> mapper.getTypes().get(new Random().nextInt(0, mapper.getTypes().size()))).toList();

        when(service.getByOrganizationId(organizationId)).thenReturn(list);

        final var expectedList = list.stream().filter(tt -> mapper.getServiceType(tt).equals(serviceType)).toList();

        final var actualList = controller.getByServiceType(serviceType, organizationId);

        assertThat(actualList).isNotNull().hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.size(); i++) {
            final var actual = actualList.get(i);
            final var expected = expectedList.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getName()).isEqualTo(expected);
                it.assertThat(actual.getRusName()).isEqualTo(mapper.getRusName(expected));
            });
        }
    }

    @Test
    @DisplayName("Получение типов транспорта организации")
    void test_getByOrganization() {
        final var organizationId = Instancio.create(UUID.class);
        final var list = IntStream.range(0, new Random().nextInt(0, 100)).mapToObj(i -> mapper.getTypes().get(new Random().nextInt(0, mapper.getTypes().size()))).toList();

        when(service.getByOrganizationId(organizationId)).thenReturn(list);

        final var actualList = controller.getAll(organizationId);

        assertThat(actualList).isNotNull().hasSameSizeAs(mapper.getTypes());

        for (var i = 0; i < actualList.size(); i++) {
            final var actual = actualList.get(i);
            final var expected = mapper.getTypes().get(i);
            assertSoftly(it -> it.assertThat(actual.getTransportType()).isEqualTo(expected));
        }
    }

    @Test
    @DisplayName("Получение типов транспорта по виду услуги")
    void test_getAllByServiceType() {
        final var serviceType = serviceTypes.get(new Random().nextInt(0, serviceTypes.size()));
        final var organizationId = Instancio.create(UUID.class);
        final var list = IntStream.range(0, new Random().nextInt(0, 100)).mapToObj(i -> mapper.getTypes().get(new Random().nextInt(0, mapper.getTypes().size()))).toList();

        final var expectedList = list.stream().filter(tt -> mapper.getServiceType(tt).equals(serviceType)).toList();

        when(service.getByOrganizationId(organizationId)).thenReturn(list);

        final var actualList = controller.getAllByServiceType(serviceType, organizationId);

        assertThat(actualList).isNotNull().hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.size(); i++) {
            final var actual = actualList.get(i);
            final var expected = expectedList.get(i);
            assertSoftly(it -> it.assertThat(actual.getTransportType()).isEqualTo(expected));
        }
    }

}