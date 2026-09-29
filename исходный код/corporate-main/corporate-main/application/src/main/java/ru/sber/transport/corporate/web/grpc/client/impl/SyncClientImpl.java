package ru.sber.transport.corporate.web.grpc.client.impl;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync.grpc.service.StateServiceGrpc;
import ru.sber.transport.corporate.web.grpc.client.SyncClient;
import ru.sber.transport.corporate.web.grpc.mappers.ImportEmployeeGrpcMapper;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Component
class SyncClientImpl implements SyncClient {

    private final Provider<Employee, EmployeeFilter> employees;

    private final ImportEmployeeGrpcMapper employeeGrpcMapper;

    @GrpcClient("easup")
    private StateServiceGrpc.StateServiceBlockingStub blockingStub;

    @Override
    public Optional<Employee> findEntered() {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            var token = authentication.getToken();
            var personnelNumber = token.getSubject();
            var id = UUID.fromString(token.getId());
            return executor.submit(() -> get(id, personnelNumber)).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            return Optional.empty();
        } catch (StatusRuntimeException e) {
            if (Status.NOT_FOUND.equals(e.getStatus())) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private Optional<Employee> get(UUID id, String personnelNumber) {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var employee = executor.submit(() -> {
                var response = blockingStub.getEmployee(State.Request.newBuilder().setId(personnelNumber).build());

                var received = employeeGrpcMapper.toBusiness(response);
                received.setId(id);
                return employees.save(received);
            }).get(1, TimeUnit.SECONDS);

            return Optional.of(employee);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            return Optional.empty();
        } catch (StatusRuntimeException e) {
            if (Status.NOT_FOUND.equals(e.getStatus())) {
                return Optional.empty();
            }
            throw e;
        }
    }
}