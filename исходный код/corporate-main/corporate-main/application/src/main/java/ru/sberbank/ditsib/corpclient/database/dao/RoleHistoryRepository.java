package ru.sberbank.ditsib.corpclient.database.dao;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.RoleHistory;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий истории назначения ролей сотруднику.
 */
public interface RoleHistoryRepository extends JpaRepository<RoleHistory, UUID> {
    
    /**
     * Получение истории роли для сотрудника.
     *
     * @param employeeId идентификатор сотрудника.
     * @return список записей истории.
     */
    List<RoleHistory> findByEmployeeId(@NotNull UUID employeeId);

}
