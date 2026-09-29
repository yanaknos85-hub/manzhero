package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;

import java.util.List;
import java.util.UUID;
/**
 * Repository for cargo group
 */
@Repository
public interface  CargoGroupRepository extends JpaRepository<CargoGroup, UUID> {

    @Query("select cg from CargoGroup cg join cg.cargoType ct where ct.active = true and ct.organization.id is null " +
            "and cg.name = :cargoGroupName")
    List<CargoGroup> findAllByGargoGroupName(String cargoGroupName);
}
