package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeDocumentRepository
        extends JpaSpecificationExecutor<EmployeeDocument>, JpaRepository<EmployeeDocument, UUID> {
    List<EmployeeDocument> findAllByEmployeeId(UUID employeeId);

    @Query("""
            SELECT COUNT(e) >= 3
            FROM EmployeeDocument e
            WHERE e.employeeId = :employeeId
            OR e.carId = :carId
            """)
    boolean existsByIdEmployeeIdAndCarId(UUID employeeId, UUID carId);

    @Query("""
            SELECT e FROM EmployeeDocument e
            JOIN e.documentType dt
            WHERE ((e.employeeId = :employeeId AND dt.documentCode = 'DRIVER_LIC')
            OR (e.carId = :carId AND dt.documentCode = 'OSAGO'))
            AND e.startTimeDocument <= :desiredDate
            AND e.finalTimeDocument > :desiredDate
            """)
    List<EmployeeDocument> findValidationDocuments(
            @Param("employeeId") UUID employeeId,
            @Param("carId") UUID carId,
            @Param("desiredDate") LocalDateTime desiredDate);

    @Query("""
            SELECT COUNT(e) > 0 
            FROM EmployeeDocument e
            JOIN e.documentType dt
            WHERE e.employeeId = :employeeId
            AND dt.documentCode = 'DRIVER_LIC'
            AND e.startTimeDocument <= :desiredDate
            AND e.finalTimeDocument > :desiredDate
            """)
    boolean isDriverLicenseValidForDate(UUID employeeId, LocalDateTime desiredDate);
}
