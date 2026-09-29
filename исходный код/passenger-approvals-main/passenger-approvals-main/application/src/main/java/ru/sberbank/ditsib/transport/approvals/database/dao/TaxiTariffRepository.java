package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;

import java.util.UUID;

public interface TaxiTariffRepository extends JpaRepository<TaxiTariff, UUID> {
}
