package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;

import java.util.List;
import java.util.UUID;

/**
 * Organization repository
 */
@Repository
@Transactional(readOnly = true)
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    /**
     * Получить список организаций по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список организаций
     */
    List<Organization> findAllByActive(boolean isActive);
    
}
