package ru.sberbank.ditsib.transport.limits.dao;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus_;

import java.util.Optional;
import java.util.UUID;

public interface BonusRepository extends JpaRepository<Bonus, UUID> {
    
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = Bonus_.REQUESTS)
    @Override
    Optional<Bonus> findById(@NonNull UUID id);
}