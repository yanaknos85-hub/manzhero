package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Organization_;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Organization repository
 */
@Repository
public interface OrganizationRepository extends JpaSpecificationExecutor<Organization>, JpaRepository<Organization,
        UUID> {

    /**
     * find organizations by offilical name
     *
     * @param name official name
     * @return organization
     */
    Optional<Organization> findByOfficialName(String name);

    /**
     * find organizations by offilical name like fragment
     *
     * @param title official name substring
     * @return qualified organizations
     */
    List<Organization> findByOfficialNameLikeIgnoreCase(String title);

    /**
     * Check if same unique data already exists.
     *
     * @param officialName organization official name
     * @param excluded     set of entity ids excluded from the check
     * @return true if entity with same data already exists
     */
    Optional<Organization> findByOfficialNameAndIdNot(final String officialName, UUID excluded);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = Organization_.CONTACTS)
    @NonNull
    Optional<Organization> findById(@NonNull UUID id);

    @Override
    @EntityGraph(attributePaths = Organization_.CONTACTS)
    @NonNull
    Page<Organization> findAll(Specification spec, @NonNull Pageable pageable);

    /**
     * Найти все организации по группе организаций
     *
     * @param organizationGroupId ID группы организаций.
     * @return список организаций.
     */
    List<Organization> findAllByOrganizationGroupId(UUID organizationGroupId);

    /**
     * Найти все организации по списку ID
     *
     * @param organizationIds список ID организаций.
     * @return список организаций.
     */
    List<Organization> findAllByIdIn(List<UUID> organizationIds);

    /**
     * Проверить, существует ли организация с указанным идентификатором синхронизации.
     *
     * @param syncId идентификатор синхронизации.
     * @return true, если организация существует.
     */
    boolean existsBySyncId(String syncId);
}
