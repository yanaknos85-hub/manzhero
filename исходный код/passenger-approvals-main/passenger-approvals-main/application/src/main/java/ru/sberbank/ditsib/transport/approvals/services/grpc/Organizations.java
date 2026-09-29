package ru.sberbank.ditsib.transport.approvals.services.grpc;


import ru.sberbank.ditsib.transport.approvals.database.model.Organization;

import java.util.UUID;

public interface Organizations {

    Organization one(UUID id);
}
