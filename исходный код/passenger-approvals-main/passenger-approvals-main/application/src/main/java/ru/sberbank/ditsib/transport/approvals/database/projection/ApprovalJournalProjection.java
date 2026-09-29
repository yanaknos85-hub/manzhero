package ru.sberbank.ditsib.transport.approvals.database.projection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ApprovalJournalProjection {

    UUID getId();

    UUID getEmployeeId();

    String getEmployeeFirstName();

    String getEmployeeLastName();

    String getEmployeePatronymic();

    String getEmployeePersonnelNumber();

    UUID getEmployeeDepartmentId();

    UUID getEmployeeUserId();

    UUID getRequestId();

    String getTransportType();

    String getTaxiClass();

    LocalDateTime getDesiredDate();

    UUID getPurposeId();

    String getPurposeLabel();

    double getCost();

    String getStatus();

    List<Map<String, Object>> getWaypoints();

    Duration getExpectedTime();

    double getExpectedDistance();

    int getPassengerCount();

    String getRequestHumanReadableId();

    boolean getIsCoopTrip();

    String getType();

    String getTimeZone();

    UUID getAddRequestId();

}
