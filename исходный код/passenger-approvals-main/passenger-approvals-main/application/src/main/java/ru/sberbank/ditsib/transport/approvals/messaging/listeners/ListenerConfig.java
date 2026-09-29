package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.mappers.*;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.*;
import ru.sberbank.ditsib.transport.approvals.messaging.message.*;
import ru.sberbank.ditsib.transport.approvals.provider.DepartmentProvider;
import ru.sberbank.ditsib.transport.approvals.provider.EmployeeProvider;
import ru.sberbank.ditsib.transport.approvals.provider.OrganizationProvider;
import ru.sberbank.ditsib.transport.approvals.provider.PositionProvider;
import ru.sberbank.ditsib.transport.approvals.services.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

@Configuration
public class ListenerConfig {

    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }

    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputDlq(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }

    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }

    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputDlq(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }

    @Bean
    public Consumer<Message<PositionMessage>> positionsInput(PositionProvider provider) {
        return getPositionConsumer(provider);
    }

    @Bean
    public Consumer<Message<PositionMessage>> positionsInputDlq(PositionProvider provider) {
        return getPositionConsumer(provider);
    }

    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInput(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }

    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputDlq(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }

    @Bean
    public Consumer<Message<DelegateMessage>> delegateInput(DelegateService delegateService, TripApproverService approverService) {
        return new DelegateListenerImpl(delegateService, approverService);
    }

    @Bean
    public Consumer<Message<LimitMessage>> limitInput(DepLimitService service, DepLimitMapper mapper) {
        return new DepLimitListenerImpl(service, mapper);
    }

    @Bean
    public Consumer<Message<LimitActionResultMessage>> limitReservationResponse(ApproveService<TripRequestApproval> approveService, LimitReservationResponseService reserveService) {
        return new LimitActionResponseListenerImpl(approveService, reserveService);
    }

    @Bean
    public Consumer<Message<RequestDocumentMessage>> requestDocumentInput(RequestDocumentMapper mapper, RequestDocumentService service) {
        return new RequestDocumentListenerImpl(mapper, service);
    }

    @Bean
    public Consumer<Message<TariffMessage>> tariffInput(TariffMapper mapper, TariffService service) {
        return new TariffListenerImpl(mapper, service);
    }

    @Bean
    public Consumer<Message<TaxiTariffMessage>> taxiTariffInput(TaxiTariffService service, TariffMapper mapper) {
        return new TaxiTariffListenerImpl(service, mapper);
    }

    @Bean
    public Consumer<Message<TripPurposeMessage>> tripPurposeInput(TripPurposeMapper mapper, TripPurposeService service) {
        return new TripPurposeListenerImpl(mapper, service);
    }

    @Bean
    public Consumer<Message<RequestMessage>> requestInput(RequestApprovalService service) {
        return new TripRequestListenerImpl(service);
    }

    @Bean
    public Consumer<Message<UpdateTripRequestMessage>> updateTripRequestInput(ApproveUpdateTripRequestService approverService, TripApprovalMapper mapper, EmployeeService service) {
        return new UpdateTripRequestListenerImpl(approverService, mapper, service);
    }

    @Bean
    public Consumer<Message<ViewDocumentMessage>> viewDocumentInput(ViewDocumentMapper mapper, RequestDocumentService service) {
        return new ViewDocumentListenerImpl(mapper, service);
    }

    @NotNull
    private Consumer<Message<OrganizationMessage>> getOrganizationConsumer(OrganizationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @NotNull
    private Consumer<Message<ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage>> getDepartmentConsumer(DepartmentProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @NotNull
    private Consumer<Message<PositionMessage>> getPositionConsumer(PositionProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @NotNull
    private Consumer<Message<EmployeeMessage>> getEmployeeConsumer(EmployeeProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
}
