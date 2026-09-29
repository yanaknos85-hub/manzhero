package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PersonalCar repository
 */
@Repository
public interface PersonalCarRepository extends JpaSpecificationExecutor<PersonalCar>, JpaRepository<PersonalCar, UUID> {
    /**
     * Get all cars of user with specified id
     * @param id id of user
     * @return list of users cars
     */
    List<PersonalCar> findAllByEmployeeId(UUID id);

    /**
     * Get first car of user with specified id
     * @param id id of user
     * @return first car
     */
    Optional<PersonalCar> findFirstByEmployeeId(UUID id);

    /**
     * Найти ЛТ по id и id пользователя
     * @param id  идентификатор ЛТ
     * @param employeeId идентификатор пользователя
     * @return ЛТ
     */
    Optional<PersonalCar> findByIdAndEmployeeId(UUID id, UUID employeeId);
}
