package ru.sberbank.ditsib.transport.limits.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.limits.reserve.backward.grpc.LimitBackward;
import ru.sber.transport.limits.reserve.backward.grpc.ResharingGrpc;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPerPeriodRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department_;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization_;
import ru.sberbank.ditsib.transport.limits.model.limit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
class ResharingServiceImpl extends ResharingGrpc.ResharingImplBase {

    private final LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;

    private final DepLimitRepository limitRepository;

    private final PlatformTransactionManager manager;

    @Override
    public void periodReserve(LimitBackward.PeriodReserveMessage request, StreamObserver<Empty> responseObserver) {
        try {
            var departmentId = UUID.fromString(request.getDepartmentId());
            var organizationId = UUID.fromString(request.getOrganizationId());
            var year = request.getYear();
            var serviceType = request.getServiceType();
            var period = Month.valueOf(request.getSource());
            var transportType = TransportTypeEnum.valueOf(request.getTransportType());
            var sum = BigDecimal.valueOf(request.getSum()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);

            new TransactionTemplate(manager).execute(status -> {

                var spec = createSpec(organizationId, departmentId, year, serviceType, transportType, period);

                limitSharingPerPeriodRepository.findOne(spec).ifPresent(periodData -> {
                    var limit = periodData.getLimitSharing().getLimit();
                    while (limit.getParent() != null) {
                        limit = limit.getParent();
                    }
                    periodData.setBalance(periodData.getBalance().subtract(sum));
                    ((DepLimit) limit).setReserve(((DepLimit) limit).getReserve().add(sum));
                    limitSharingPerPeriodRepository.save(periodData);
                    limitRepository.save(((DepLimit) limit));
                });
                return status;
            });

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private static @NotNull Specification<LimitSharingPerPeriod> createSpec(UUID organizationId, UUID departmentId, int year, String serviceType, TransportTypeEnum transportType, Period period) {
        return (root, query, cb) -> {
            var sharings = root.join(LimitSharingPerPeriod_.limitSharing);
            var limit = sharings.join(LimitSharing_.LIMIT);
            var organization = limit.join(DepLimit_.ORGANIZATION);
            var department = limit.join(DepLimit_.DEPARTMENT);
            var predicate = cb.equal(organization.get(Organization_.ID), organizationId);
            predicate = cb.and(predicate, cb.equal(department.get(Department_.ID), departmentId));
            predicate = cb.and(predicate, cb.equal(limit.get(DepLimit_.YEAR), year));
            predicate = cb.and(predicate, cb.equal(limit.get(DepLimit_.LIMIT_SERVICE_TYPE), serviceType));
            predicate = cb.and(predicate, cb.equal(limit.get(DepLimit_.LIMIT_SHARING_TYPE), LimitSharingType.MONTHLY));
            predicate = cb.and(predicate, cb.equal(sharings.get(LimitSharing_.transportType), transportType));
            predicate = cb.and(predicate, cb.equal(root.get(LimitSharingPerPeriod_.periodData), PeriodData.valueOf(period.name())));
            return predicate;
        };
    }

    @Override
    public void periodPeriod(LimitBackward.PeriodPeriodMessage request, StreamObserver<Empty> responseObserver) {
        try {
            var departmentId = UUID.fromString(request.getDepartmentId());
            var organizationId = UUID.fromString(request.getOrganizationId());
            var year = request.getYear();
            var serviceType = request.getServiceType();
            var source = Month.valueOf(request.getSource());
            var target = Month.valueOf(request.getTarget());
            var transportType = TransportTypeEnum.valueOf(request.getTransportType());
            var sum = BigDecimal.valueOf(request.getSum()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);

            var sourceSpec = createSpec(organizationId, departmentId, year, serviceType, transportType, source);
            var targetSpec = createSpec(organizationId, departmentId, year, serviceType, transportType, target);

            new TransactionTemplate(manager).execute(status -> {
                limitSharingPerPeriodRepository.findOne(sourceSpec).ifPresent(sourcePeriod ->
                    limitSharingPerPeriodRepository.findOne(targetSpec).ifPresent(targetPeriod -> {
                        sourcePeriod.setBalance(sourcePeriod.getBalance().subtract(sum));
                        targetPeriod.setBalance(targetPeriod.getBalance().add(sum));
                        limitSharingPerPeriodRepository.save(sourcePeriod);
                        limitSharingPerPeriodRepository.save(targetPeriod);
                    }));
                return status;
            });

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

}
