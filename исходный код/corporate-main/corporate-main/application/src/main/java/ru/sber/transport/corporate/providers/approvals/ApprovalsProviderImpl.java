package ru.sber.transport.corporate.providers.approvals;

import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.providers.ApprovalsProvider;
import ru.sber.transport.database.corporate_approvals.tables.Approvals;
import ru.sber.transport.database.corporate_approvals.tables.records.ApprovalsRecord;

import java.util.UUID;

@Repository
class ApprovalsProviderImpl implements ApprovalsProvider, JooqRepository<Approvals, ApprovalsRecord, UUID> {

    @Override
    public Integer countOfEmployee(UUID employeeId) {
        return context().fetchCount(context().selectFrom(table()).where(table().EMPLOYEE_ID.eq(employeeId)));
    }

    @Override
    public Approvals table() {
        return Approvals.APPROVALS;
    }
}
