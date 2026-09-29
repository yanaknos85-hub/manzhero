package ru.sberbank.ditsib.transport.approvals.services.grpc;


import ru.sberbank.ditsib.transport.approvals.database.model.Department;

import java.util.UUID;

public interface Departments {

    Department one(UUID id);
}
