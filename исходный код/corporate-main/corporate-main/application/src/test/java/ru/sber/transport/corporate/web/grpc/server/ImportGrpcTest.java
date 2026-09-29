package ru.sber.transport.corporate.web.grpc.server;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;
import ru.sber.transport.corporate.web.grpc.mappers.*;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка импорта")
class ImportGrpcTest {

    private final ActiveStatusGrpcMapper activeStatusMapper = new ActiveStatusGrpcMapperImpl();

    private final NullableMapper nullableMapper = new NullableMapperImpl();

    private final List<ImportGrpcMapper<?, ?>> mappers = List.of(
            new ImportDepartmentGrpcMapperImpl(nullableMapper, activeStatusMapper),
            new ImportPositionGrpcMapperImpl(nullableMapper, activeStatusMapper),
            new ImportEmployeeGrpcMapperImpl(activeStatusMapper, new ItinerantGrpcMapperImpl(), nullableMapper, new GenderGrpcMapperImpl(), new DateGrpcMapperImpl())
    );

    private final List<Provider<?, ?>> providers = List.of();

    private final AtomicReference<Object> bean = new AtomicReference<>();

    private final ImportGrpc importGrpc = new ImportGrpc(mappers, providers) {

        @Override
        <M extends HasOrganizationStructure, F extends Filter> Provider<M, F> getProvider(Class<M> clazz, Class<F> filterClazz) {
            return ReflectionUtils.cast(bean.get());
        }

    };

    @Test
    @DisplayName("Проверка импорта должностей. Минимум данных")
    void test_sync_positions_min() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var ids = IntStream.range(0, 10).mapToObj(i -> UUID.randomUUID()).toList();
        var index = new AtomicInteger(0);

        when(provider.saveAll(any())).thenAnswer(i -> {
            var argument = i.getArgument(0, List.class);
            argument.stream().forEach(e -> ((Position) e).setId(ids.get(index.getAndIncrement())));
            return argument;
        });

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncPosition(outputStream);
        var expectedList = new ArrayList<Import.PositionRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.PositionRequest.newBuilder()
                    .setSyncId(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setNoApproveRequired(Instancio.create(Boolean.class))
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Position>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI));
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.isNoApproveRequired()).isEqualTo(expected.getNoApproveRequired());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI).toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Проверка импорта должностей. Полные данные")
    void test_sync_positions_full() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(provider.saveAll(any())).thenAnswer(i -> i.getArgument(0, List.class));

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncPosition(outputStream);
        var expectedList = new ArrayList<Import.PositionRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.PositionRequest.newBuilder()
                    .setSyncId(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setNoApproveRequired(Instancio.create(Boolean.class))
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .setId(Import.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Position>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId().toString()).isEqualTo(expected.getId().getValue());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.isNoApproveRequired()).isEqualTo(expected.getNoApproveRequired());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Проверка импорта сотрудников. Минимум данных")
    void test_sync_employees_min() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var ids = IntStream.range(0, 10).mapToObj(i -> UUID.randomUUID()).toList();
        var index = new AtomicInteger(0);

        when(provider.saveAll(any())).thenAnswer(i -> {
            var argument = i.getArgument(0, List.class);
            argument.stream().forEach(e -> ((Employee) e).setId(ids.get(index.getAndIncrement())));
            return argument;
        });

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncEmployee(outputStream);
        var expectedList = new ArrayList<Import.EmployeeRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.EmployeeRequest.newBuilder()
                    .setPersonnelNumber(Instancio.create(String.class))
                    .setFirstName(Instancio.create(String.class))
                    .setLastName(Instancio.create(String.class))
                    .setConsent(Instancio.create(Boolean.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setDepartmentId(UUID.randomUUID().toString())
                    .setPositionId(UUID.randomUUID().toString())
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .setGender(Import.Gender.valueOf(Instancio.create(Gender.class).name()))
                    .setItinerant(Import.Itinerant.valueOf(Instancio.create(ItinerantType.class).name()))
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Employee>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getPersonnelNumber());
                it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getPersonnelNumber());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI));
                it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
                it.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
                it.assertThat(actual.isConsent()).isEqualTo(expected.getConsent());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.getDepartmentId().toString()).isEqualTo(expected.getDepartmentId());
                it.assertThat(actual.getPositionId().toString()).isEqualTo(expected.getPositionId());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getGender().name()).isEqualTo(expected.getGender().name());
                it.assertThat(actual.getItinerant().name()).isEqualTo(expected.getItinerant().name());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI).toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Проверка импорта сотрудников. Полные данные")
    void test_sync_employees_full() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(provider.saveAll(any())).thenAnswer(i -> i.getArgument(0, List.class));

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncEmployee(outputStream);
        var expectedList = new ArrayList<Import.EmployeeRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.EmployeeRequest.newBuilder()
                    .setPersonnelNumber(Instancio.create(String.class))
                    .setFirstName(Instancio.create(String.class))
                    .setLastName(Instancio.create(String.class))
                    .setConsent(Instancio.create(Boolean.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setDepartmentId(UUID.randomUUID().toString())
                    .setPositionId(UUID.randomUUID().toString())
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .setGender(Import.Gender.valueOf(Instancio.create(Gender.class).name()))
                    .setItinerant(Import.Itinerant.valueOf(Instancio.create(ItinerantType.class).name()))
                    .setId(Import.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                    .setSupervisorId(Import.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                    .setEmail(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setFireDate(Import.NullableDate.newBuilder().setValue(Import.Date.newBuilder().setYear(2000).setMonth(2).setDay(15).build()).build())
                    .setMobilePhone(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setPatronymic(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setExternalEmail(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setRoom(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setCostCenter(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setMarriageCertificate(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Employee>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getPersonnelNumber());
                it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getPersonnelNumber());
                it.assertThat(actual.getId().toString()).isEqualTo(expected.getId().getValue());
                it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
                it.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
                it.assertThat(actual.isConsent()).isEqualTo(expected.getConsent());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.getDepartmentId().toString()).isEqualTo(expected.getDepartmentId());
                it.assertThat(actual.getPositionId().toString()).isEqualTo(expected.getPositionId());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getGender().name()).isEqualTo(expected.getGender().name());
                it.assertThat(actual.getItinerant().name()).isEqualTo(expected.getItinerant().name());
                it.assertThat(actual.getEmail()).isEqualTo(expected.getEmail().getValue());
                it.assertThat(actual.getPhone()).isEqualTo(expected.getMobilePhone().getValue());
                it.assertThat(actual.getPatronymic()).isEqualTo(expected.getPatronymic().getValue());
                it.assertThat(actual.getSupervisorId().toString()).isEqualTo(expected.getSupervisorId().getValue());
                it.assertThat(actual.getRoom()).isEqualTo(expected.getRoom().getValue());
                it.assertThat(actual.getCostCenter()).isEqualTo(expected.getCostCenter().getValue());
                it.assertThat(actual.getMarriageCertificate()).isEqualTo(expected.getMarriageCertificate().getValue());
                it.assertThat(actual.getFireDate().getYear()).isEqualTo(expected.getFireDate().getValue().getYear());
                it.assertThat(actual.getFireDate().getMonthValue()).isEqualTo(expected.getFireDate().getValue().getMonth());
                it.assertThat(actual.getFireDate().getDayOfMonth()).isEqualTo(expected.getFireDate().getValue().getDay());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Проверка импорта подразделений. Минимум данных")
    void test_sync_departments_min() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var ids = IntStream.range(0, 10).mapToObj(i -> UUID.randomUUID()).toList();
        var index = new AtomicInteger(0);

        when(provider.saveAll(any())).thenAnswer(i -> {
            var argument = i.getArgument(0, List.class);
            argument.stream().forEach(e -> ((Department) e).setId(ids.get(index.getAndIncrement())));
            return argument;
        });

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncDepartment(outputStream);
        var expectedList = new ArrayList<Import.DepartmentRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.DepartmentRequest.newBuilder()
                    .setSyncId(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setCode(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Department>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI));
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.getCode()).isEqualTo(expected.getCode());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(ids.get(finalI).toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Проверка импорта подразделений. Полные данные")
    void test_sync_departments_full() throws InterruptedException {
        var provider = mock(Provider.class);

        bean.set(provider);

        var data = new CopyOnWriteArrayList<Import.SyncResponse>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        when(provider.saveAll(any())).thenAnswer(i -> i.getArgument(0, List.class));

        var outputStream = new StreamObserver<Import.SyncResponse>() {

            @Override
            public void onNext(Import.SyncResponse value) {
                data.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        var inputStream = importGrpc.syncDepartment(outputStream);
        var expectedList = new ArrayList<Import.DepartmentRequest>();

        for (var i = 0; i < 10; i++) {
            var request = Import.DepartmentRequest.newBuilder()
                    .setSyncId(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setOrganizationId(UUID.randomUUID().toString())
                    .setCode(Instancio.create(String.class))
                    .setName(Instancio.create(String.class))
                    .setStatus(Import.Active.valueOf(Instancio.create(Active.class).name()))
                    .setId(Import.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                    .setHead(Import.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                    .setLevelCode(Import.NullableInt.newBuilder().setValue(Instancio.create(Integer.class)).build())
                    .setLevelName(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setLocation(Import.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                    .setParentId(Import.NullableString.newBuilder().setValue(Instancio.create(UUID.class).toString()).build())
                    .build();
            expectedList.add(request);
            inputStream.onNext(request);
        }
        inputStream.onCompleted();

        var savedCaptor = ArgumentCaptor.forClass(List.class);

        verify(provider, times(10)).saveAll(savedCaptor.capture());

        var savedList = savedCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var saved = new ArrayList<Department>(savedList);
        assertThat(saved).hasSize(10);
        for (var i = 0; i < 10; i++) {
            var actual = saved.get(i);
            var expected = expectedList.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId().toString()).isEqualTo(expected.getId().getValue());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getStructureType()).isEqualTo(StructureType.INTERNAL);
                it.assertThat(actual.getOrganizationId().toString()).isEqualTo(expected.getOrganizationId());
                it.assertThat(actual.getHeadId().toString()).isEqualTo(expected.getHead().getValue());
                it.assertThat(actual.getParentId().toString()).isEqualTo(expected.getParentId().getValue());
                it.assertThat(actual.getLocation()).isEqualTo(expected.getLocation().getValue());
                it.assertThat(actual.getLevelCode()).isEqualTo(String.valueOf(expected.getLevelCode().getValue()));
                it.assertThat(actual.getLevelName()).isEqualTo(expected.getLevelName().getValue());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            });
        }
        assertThat(data).hasSameSizeAs(saved);
        for (var i = 0; i < 10; i++) {
            var actual = data.get(i);
            var expected = saved.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getSyncId()).isEqualTo(expected.getSyncId());
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
            });
        }
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

}
