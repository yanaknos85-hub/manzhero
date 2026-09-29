package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.FleetOwnerOrganization;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;
import java.util.UUID;

/**
 * Repository of fleet owner organization
 */
@Repository
public interface FleetOwnerOrganizationRepository extends JpaRepository<FleetOwnerOrganization, UUID> {
    
    @Query(value = """
                    select foo.organization_id, o.official_name
                    from tariff_fleet.fleet_owner_organization foo
                             join tariff_fleet.organization o on foo.organization_id = o.id
                    where o.active is true
                    order by o.official_name
                   """,
           nativeQuery = true)
    List<GetAllActiveOrganizationNamesDto> findAllActive();
}
