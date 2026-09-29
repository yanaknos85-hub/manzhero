package ru.sberbank.ditsib.transport.approvals.services.grpc;


import ru.sberbank.ditsib.transport.approvals.database.model.Position;

import java.util.UUID;

public interface Positions {

    Position one(UUID id);
}
