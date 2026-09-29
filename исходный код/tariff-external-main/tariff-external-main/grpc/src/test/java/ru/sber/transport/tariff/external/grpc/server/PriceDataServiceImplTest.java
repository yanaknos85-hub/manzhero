package ru.sber.transport.tariff.external.grpc.server;

import com.google.protobuf.NullValue;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.business.Links;
import ru.sber.transport.tariff.external.business.Tariffs;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка работы логики gRPC-сервера")
class PriceDataServiceImplTest {

    private final Tariffs tariffs = mock(Tariffs.class);

    private final Links links = mock(Links.class);

    private final PriceDataServiceImpl priceDataServiceImpl = new PriceDataServiceImpl(tariffs, links);

    @Test
    @DisplayName("Проверка вызова метода получения данных о тарифах")
    void test_getTariffData() {
        final var uri = URI.create("https://3.redirect.appmetrica.yandex.com/route?start-lat=55.73400123907955&start-lon=37.588533418821726&end-lat=55.76776211471192&end-lon=37.60714921124336&tariffClass=econom&ref=yoursiteru&appmetrica_tracking_id=1178268795219780156");

        when(links.get(anyList(), eq(Type.COMFORT))).thenReturn(uri);

        final var request = ExternalTariff.PriceDataRequest.newBuilder()
                .addTariff(ExternalTariff.Tariff.COMFORT)
                .setStart(ExternalTariff.Coordinates.newBuilder().setLatitude(Instancio.create(Double.class)).setLongitude(Instancio.create(Double.class)).build())
                .setEnd(ExternalTariff.Coordinates.newBuilder().setLatitude(Instancio.create(Double.class)).setLongitude(Instancio.create(Double.class)).build())
                .build();

        final var result = new AtomicReference<ExternalTariff.PriceDataResponse>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicBoolean();

        final var response = new StreamObserver<ExternalTariff.PriceDataResponse>() {

            @Override
            public void onNext(ExternalTariff.PriceDataResponse priceDataResponse) {
                result.set(priceDataResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        final var tariffList = Instancio.ofList(TestTariff.class)
                .size(1)
                .create()
                .stream()
                .map(Tariff.class::cast)
                .toList();

        when(tariffs.get(anyList(), anyList())).thenReturn(tariffList);

        priceDataServiceImpl.request(request, response);

        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
        assertThat(result.get()).isNotNull();

        final var actual = result.get();

        assertSoftly(it -> {
            final var tariff = tariffList.get(0);
            final var expectedPriceInt = tariff.price().intValue();
            final var expectedPriceFraction = tariff.price().subtract(BigDecimal.valueOf(expectedPriceInt)).multiply(BigDecimal.TEN).intValue();

            it.assertThat(actual.getCost().getIntegerPart()).isEqualTo(expectedPriceInt);
            it.assertThat(actual.getCost().getFractionPart()).isEqualTo(expectedPriceFraction);
            it.assertThat(actual.getDistance()).isEqualTo(tariff.distance());
            it.assertThat(actual.getTime()).isEqualTo(tariff.time().toString());
            it.assertThat(actual.getWaitTime()).isEqualTo(tariff.waitTime().toString());
            it.assertThat(actual.getLink().getValue()).isEqualTo(uri.toASCIIString());
        });
    }

    @Test
    @DisplayName("Проверка вызова метода получения данных о тарифах. Запрошено несколько тарифов")
    void test_getTariffData_several() {
        final var request = ExternalTariff.PriceDataRequest.newBuilder()
                .addTariff(ExternalTariff.Tariff.COMFORT)
                .addTariff(ExternalTariff.Tariff.ECONOMY)
                .setStart(ExternalTariff.Coordinates.newBuilder().setLatitude(Instancio.create(Double.class)).setLongitude(Instancio.create(Double.class)).build())
                .setEnd(ExternalTariff.Coordinates.newBuilder().setLatitude(Instancio.create(Double.class)).setLongitude(Instancio.create(Double.class)).build())
                .build();

        final var result = new AtomicReference<ExternalTariff.PriceDataResponse>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicBoolean();

        final var response = new StreamObserver<ExternalTariff.PriceDataResponse>() {

            @Override
            public void onNext(ExternalTariff.PriceDataResponse priceDataResponse) {
                result.set(priceDataResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        final var tariffList = Instancio.ofList(TestTariff.class)
                .size(1)
                .create()
                .stream()
                .map(Tariff.class::cast)
                .toList();

        when(tariffs.get(anyList(), anyList())).thenReturn(tariffList);

        priceDataServiceImpl.request(request, response);

        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
        assertThat(result.get()).isNotNull();

        final var actual = result.get();

        assertSoftly(it -> {
            final var tariff = tariffList.get(0);
            final var expectedPriceInt = tariff.price().intValue();
            final var expectedPriceFraction = tariff.price().subtract(BigDecimal.valueOf(expectedPriceInt)).multiply(BigDecimal.TEN).intValue();

            it.assertThat(actual.getCost().getIntegerPart()).isEqualTo(expectedPriceInt);
            it.assertThat(actual.getCost().getFractionPart()).isEqualTo(expectedPriceFraction);
            it.assertThat(actual.getDistance()).isEqualTo(tariff.distance());
            it.assertThat(actual.getTime()).isEqualTo(tariff.time().toString());
            it.assertThat(actual.getWaitTime()).isEqualTo(tariff.waitTime().toString());
            it.assertThat(actual.getLink().getNull()).isEqualTo(NullValue.NULL_VALUE);
        });
    }

    private record TestTariff(Type type, long distance, BigDecimal price, Duration time, Duration waitTime) implements Tariff {}

}