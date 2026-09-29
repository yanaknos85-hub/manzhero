package ru.sber.transport.limits.providers;

import org.instancio.Instancio;
import ru.sber.transport.database.limits.enums.*;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.database.limits.tables.records.LimitSharingPerPeriodRecord;
import ru.sber.transport.database.limits.tables.records.SharingsRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface DataCreator {

    default LimitRecord createLimit(UUID organizationId, String humanReadableId) {
        var data = new LimitRecord();
        data.setLimitStatus(Instancio.create(Status.class));
        data.setId(Instancio.create(UUID.class));
        data.setCreationTime(Instancio.create(OffsetDateTime.class));
        data.setAuthorId(Instancio.create(UUID.class));
        data.setDepartmentId(Instancio.create(UUID.class));
        data.setEconomy(Instancio.create(BigDecimal.class));
        data.setFinalSharing(Instancio.create(Boolean.class));
        data.setUseMyLimit(Instancio.create(Boolean.class));
        data.setHumanReadableId(humanReadableId);
        data.setLimitOwnerId(Instancio.create(UUID.class));
        data.setServiceType("PASSENGER");
        data.setLimitSharingType(Instancio.create(SharingType.class));
        data.setLimitType(Instancio.create(Type.class));
        data.setOrganizationId(organizationId);
        data.setReserve(Instancio.create(BigDecimal.class));
        data.setSum(Instancio.create(BigDecimal.class));
        data.setYear(Instancio.create(Integer.class));
        data.setUpdateTime(Instancio.create(OffsetDateTime.class));
        data.setHash(Instancio.create(String.class));
        return data;
    }

    default LimitRecord createLimit() {
        return createLimit(UUID.randomUUID());
    }

    default LimitRecord createLimit(UUID organizationId) {
        return createLimit(organizationId, Instancio.create(String.class));
    }

    default LimitRecord createLimit(UUID parentId, UUID organizationId) {
        var data = createLimit(organizationId);
        data.setParentId(parentId);
        return data;
    }

    default LimitRecord createLimit(UUID organization, UUID department, int year, ru.sber.transport.limits.business.model.Status status, String serviceType) {
        var data = new LimitRecord();
        data.setLimitStatus(Status.valueOf(status.name()));
        data.setId(Instancio.create(UUID.class));
        data.setCreationTime(Instancio.create(OffsetDateTime.class));
        data.setAuthorId(Instancio.create(UUID.class));
        data.setDepartmentId(Instancio.create(UUID.class));
        data.setEconomy(Instancio.create(BigDecimal.class));
        data.setFinalSharing(Instancio.create(Boolean.class));
        data.setUseMyLimit(Instancio.create(Boolean.class));
        data.setHumanReadableId(Instancio.create(String.class));
        data.setLimitOwnerId(Instancio.create(UUID.class));
        data.setServiceType(serviceType);
        data.setLimitSharingType(Instancio.create(SharingType.class));
        data.setLimitType(Instancio.create(Type.class));
        data.setOrganizationId(organization);
        data.setDepartmentId(department);
        data.setReserve(Instancio.create(BigDecimal.class));
        data.setSum(Instancio.create(BigDecimal.class));
        data.setYear(year);
        data.setUpdateTime(Instancio.create(OffsetDateTime.class));
        data.setHash(Instancio.create(String.class));
        return data;
    }

    default LimitRecord createLimit(LimitRecord parent) {
        var data = createLimit();
        data.setParentId(parent.getId());
        return data;
    }

    default SharingsRecord createLimitSharing(LimitRecord limit, EmployeeRecord author) {
        return createLimitSharing(limit, author, "TAXI");
    }

    default SharingsRecord createLimitSharing(LimitRecord limit, EmployeeRecord author, String transportType) {
        var data = new SharingsRecord();
        data.setId(Instancio.create(UUID.class));
        data.setLimitId(limit.getId());
        data.setSum(Instancio.create(BigDecimal.class));
        data.setRemains(Instancio.create(BigDecimal.class));
        data.setCreationTime(LocalDateTime.now());
        data.setAuthorId(author.getId());
        data.setTransportType(transportType);
        return data;
    }

    default LimitSharingPerPeriodRecord createLimitSharingPerPeriod(SharingsRecord limitSharing, EmployeeRecord author) {
        return createLimitSharingPerPeriod(limitSharing, author, Instancio.create(Period.class));
    }

    default LimitSharingPerPeriodRecord createLimitSharingPerPeriod(SharingsRecord limitSharing, EmployeeRecord author, Period period){
        return createLimitSharingPerPeriod(limitSharing, author, period, false);
    }

    default LimitSharingPerPeriodRecord createLimitSharingPerPeriod(SharingsRecord limitSharing, EmployeeRecord author, Period period, Boolean wasMoved) {
        var data = new LimitSharingPerPeriodRecord();
        data.setId(Instancio.create(UUID.class));
        data.setLimitSharingId(limitSharing.getId());
        data.setPeriod(period);
        data.setSum(Instancio.create(BigDecimal.class));
        data.setBalance(Instancio.create(BigDecimal.class));
        data.setCreationTime(LocalDateTime.now());
        data.setAuthorId(author.getId());
        data.setAdditional(Instancio.create(BigDecimal.class));
        data.setMovedToNext(wasMoved);
        return data;
    }

    default EmployeeRecord createEmployee() {
        var data = new EmployeeRecord();
        data.setId(Instancio.create(UUID.class));
        data.setDepartmentId(Instancio.create(UUID.class));
        data.setOrganizationId(Instancio.create(UUID.class));
        data.setUserId(Instancio.create(UUID.class));
        data.setEmail(Instancio.create(String.class));
        return data;
    }

}
