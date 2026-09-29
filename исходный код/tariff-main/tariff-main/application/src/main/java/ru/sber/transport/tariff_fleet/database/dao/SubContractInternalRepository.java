package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.tariff_fleet.database.model.AbstractContract;

import java.util.UUID;

public interface SubContractInternalRepository<E extends AbstractContract> extends JpaRepository<E, UUID> {
}