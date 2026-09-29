package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.OrganizationGroup;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationGroupRepository extends JpaRepository<OrganizationGroup, UUID>, JpaSpecificationExecutor<OrganizationGroup> {

    Optional<OrganizationGroup> findByName(String name);

}
