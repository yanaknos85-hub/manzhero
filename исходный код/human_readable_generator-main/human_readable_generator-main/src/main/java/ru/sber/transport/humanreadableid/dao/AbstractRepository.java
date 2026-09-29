package ru.sber.transport.humanreadableid.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;

import java.util.UUID;

/**
 * Abstract repository - it need because table my exist in diff DB schemas
 *
 * @param <T> тип последовательности.
 */
@NoRepositoryBean
public interface AbstractRepository<T extends BaseCompanySQ> extends JpaRepository<T, UUID> {

    /**
     * Найти владельца последовательности.
     *
     * @param prefix префикс последовательности.
     * @param organizationId корневой идентификатор.
     *
     * @return значение последовательности.
     */
    @Query("SELECT a FROM #{#entityName} a WHERE a.prefix = :prefix AND a.orgDigitId=:organizationId")
    T findByPrefixAndOrgDigitIdForWrite(
            @Param("prefix") String prefix, @Param("organizationId") Long organizationId
                                       );

    /**
     * Найти владельца последовательности.
     *
     * @param prefix префикс последовательности.
     * @param organizationId корневой идентификатор.
     *
     * @return значение последовательности.
     */
    T findByPrefixAndOrgDigitId(String prefix, Long organizationId);

    /**
     * Найти владельца последовательности.
     *
     * @param prefix префикс последовательности.
     * @param organizationId корневой идентификатор.
     *
     * @return флаг существования префикса.
     */
    boolean existsByPrefixAndOrgDigitId(String prefix, Long organizationId);
}