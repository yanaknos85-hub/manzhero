package ru.sber.transport.tariff.external.grpc.server;

import com.google.protobuf.NullValue;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;
import ru.sber.transport.tariff.external.business.Links;
import ru.sber.transport.tariff.external.business.Tariffs;
import ru.sber.transport.tariff.external.grpc.server.model.GrpcCoordinates;
import ru.sber.transport.tariff.external.model.Type;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для предоставления данных о тарифах.
 */
@RequiredArgsConstructor
public class PriceDataServiceImpl extends PriceDataServiceGrpc.PriceDataServiceImplBase {

    private final Tariffs tariffs;

    private final Links links;

    @Override
    public void request(ExternalTariff.PriceDataRequest request, StreamObserver<ExternalTariff.PriceDataResponse> responseObserver) {
        final var coordinates = List.of(new GrpcCoordinates(request.getStart()), new GrpcCoordinates(request.getEnd()));
        final var tariffTypes = request.getTariffList().parallelStream().map(Enum::name).map(Type::valueOf).toList();
        var link = Optional.<URI>empty();
        if (tariffTypes.size() == 1) {
            link = Optional.of(links.get(coordinates, tariffTypes.get(0)));
        }
        try {
            final var tariff = this.tariffs.get(coordinates, tariffTypes).get(0);
            if (tariff != null) {
                responseObserver.onNext(ExternalTariff.PriceDataResponse.newBuilder()
                        .setCost(toGrpc(tariff.price()))
                        .setTime(tariff.time().toString())
                        .setWaitTime(tariff.waitTime().toString())
                        .setDistance(tariff.distance())
                        .setLink(toGrpc(link.orElse(null)))
                        .build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
            }
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.RESOURCE_EXHAUSTED));
        }
    }

    private ExternalTariff.NullableString toGrpc(URI link) {
        return Optional.ofNullable(link)
                .map(URI::toASCIIString)
                .map(it -> ExternalTariff.NullableString.newBuilder().setValue(it).build())
                .orElseGet(() -> ExternalTariff.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }

    private ExternalTariff.Cost toGrpc(BigDecimal price) {
        final var integerPart = price.intValue();
        return ExternalTariff.Cost.newBuilder()
                .setIntegerPart(integerPart)
                .setFractionPart(price.subtract(BigDecimal.valueOf(integerPart)).multiply(BigDecimal.TEN).intValue())
                .build();
    }
}
