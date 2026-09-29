package ru.sber.transport.corporate.web.grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;
import ru.sber.transport.corporate.sync_import.grpc.service.SyncServiceGrpc;
import ru.sber.transport.corporate.web.grpc.mappers.ImportGrpcMapper;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Slf4j
@GrpcService
class ImportGrpc extends SyncServiceGrpc.SyncServiceImplBase {

    private final List<ImportGrpcMapper<?, ?>> mappers;

    private final List<Provider<?, ?>> providers;

    @Override
    public StreamObserver<Import.PositionRequest> syncPosition(StreamObserver<Import.SyncResponse> responseObserver) {
        return doHandle(responseObserver, Import.PositionRequest.class, Position.class, PositionFilter.class);
    }

    @Override
    public StreamObserver<Import.DepartmentRequest> syncDepartment(StreamObserver<Import.SyncResponse> responseObserver) {
        return doHandle(responseObserver, Import.DepartmentRequest.class, Department.class, DepartmentFilter.class);
    }

    @Override
    public StreamObserver<Import.EmployeeRequest> syncEmployee(StreamObserver<Import.SyncResponse> responseObserver) {
        return doHandle(responseObserver, Import.EmployeeRequest.class, Employee.class, EmployeeFilter.class);
    }

    private <T, M extends HasOrganizationStructure, F extends Filter> StreamObserver<T> doHandle(StreamObserver<Import.SyncResponse> responseObserver, Class<T> sourceClass, Class<M> businessClass, Class<F> filterClass) {
        var mapper = getMapper(sourceClass, businessClass);
        var provider = getProvider(businessClass, filterClass);
        return new StreamObserver<>() {

            private final List<M> buffer = new CopyOnWriteArrayList<>();

            private Future<?> task = null;

            @Override
            public void onNext(T value) {
                buffer.add(mapper.toBusiness(value));
                if (task == null) {
                    try (var executor = Executors.newSingleThreadExecutor()) {
                        task = executor.submit(() -> {
                            var toSave = new ArrayList<>(buffer);
                            buffer.clear();
                            try {
                                provider.saveAll(toSave)
                                        .stream()
                                        .map(mapper::toGrpc)
                                        .forEach(responseObserver::onNext);
                            } catch (Exception e) {
                                onError(e);
                            } finally {
                                task = null;
                            }
                        });
                    } catch (Exception e) {
                        onError(e);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Synchronizing failed", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private <T, M extends HasOrganizationStructure> ImportGrpcMapper<T, M> getMapper(Class<T> sourceClass, Class<M> businessClass) {
        return ReflectionUtils.cast(getBean(mappers, ImportGrpcMapper.class, sourceClass, businessClass));
    }

    <M extends HasOrganizationStructure, F extends Filter> Provider<M, F> getProvider(Class<M> clazz, Class<F> filter) {
        return ReflectionUtils.cast(getBean(providers, Provider.class, clazz, filter));
    }

    private <B> B getBean(List<?> beans, Class<B> beanClass, Class<?>... classes) {
        var type = ResolvableType.forClassWithGenerics(beanClass, classes);
        var bean = beans.parallelStream().filter(m -> type.isAssignableFrom(m.getClass())).findAny().orElseThrow(() -> new NoSuchBeanDefinitionException(type));
        return ReflectionUtils.cast(bean);
    }
}
