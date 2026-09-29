package ru.sber.transport.tariff_fleet.database.projection;

import java.util.UUID;

public interface GetEwbTariffByIdProjection {
    
    UUID getId();
    
    String getInspectionType();
    
    String getFleetOwnerName();
    
    String getFleetOwnerDepartmentName();
    
    String getContractorName();
    
    String getContractNumber();
    
    Long getAmount();
    
    
    void setId(UUID value);
    
    void setInspectionType(String value);
    
    void setFleetOwnerName(String value);
    
    void setFleetOwnerDepartmentName(String value);
    
    void setContractorName(String value);
    
    void setContractNumber(String value);
    
    void setAmount(Long value);
    
}
