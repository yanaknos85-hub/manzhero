package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettingsItem;

import java.util.UUID;

/**
 * JPA репозитарий элементов настроек совместных поездок
 */
@Repository
public interface SharedRideSettingsItemRepository extends JpaRepository<SharedRideSettingsItem, UUID> {
}
