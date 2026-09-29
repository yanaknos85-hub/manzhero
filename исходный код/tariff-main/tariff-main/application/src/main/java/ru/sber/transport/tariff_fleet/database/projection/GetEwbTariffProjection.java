package ru.sber.transport.tariff_fleet.database.projection;

import java.util.UUID;

public interface GetEwbTariffProjection {
    UUID getId();
    
    String getHumanReadableId();
    
    boolean getActive();
    
    String getInspectionType();
    
    String getOrganizationName();
    
    String getDepartmentName();
    
    String getContractOrganizationName();
    
    String getContractNumber();
    
    void setId(UUID id);
    
    void setHumanReadableId(String value);
    
    void setActive(boolean value);
    
    void setInspectionType(String value);
    
    void setOrganizationName(String value);
    
    void setDepartmentName(String value);
    
    void setContractOrganizationName(String value);
    
    void setContractNumber(String value);
}