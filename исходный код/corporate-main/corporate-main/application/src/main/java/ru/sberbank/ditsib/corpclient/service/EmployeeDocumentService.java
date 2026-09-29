package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Employee document service. crud and other operations
 */
public interface EmployeeDocumentService {
    /**
     * Система выбирает транспортное средство по employee_id и car_id из таблицы employee_document.
     * Проверяет наличие связи между сотрудником и конкретным автомобилем.
     *
     * @param employeeId идентификатор пользователя.
     * @param carId идентификатор транспортного средства.
     *
     * @return булево значение.
     */
    boolean hasEmployeeAccessToCar(UUID employeeId, UUID carId);

    /**
     * Система выбирает все документы сотрудника для указанного ТС
     * по employee_id, car_id и document_code IN ('DRIVER_LIC', 'OSAGO') на указанную дату.
     * Используется для проверки актуальности прав и страховки.
     *
     * @param employeeId идентификатор пользователя.
     * @param carId идентификатор транспортного средства
     * @param desiredDate Момент времени (дата и время поездки)
     *
     * @return Список документов сотрудника для указанного ТС(Водительский, ОСАГО) на момент времени.
     */
    List<EmployeeDocument> findValidationDocuments(UUID employeeId, UUID carId, LocalDateTime desiredDate);

    /**
     * Проверяет наличие у сотрудника действующих водительских прав на указанную дату.
     *
     * @param employeeId идентификатор сотрудника.
     * @param desiredDate момент времени (дата и время поездки).
     * @return true, если у сотрудника есть действующие водительские права на указанную дату.
     */
    boolean hasEmployeeActiveDriverLicense(UUID employeeId, LocalDateTime desiredDate);
}
