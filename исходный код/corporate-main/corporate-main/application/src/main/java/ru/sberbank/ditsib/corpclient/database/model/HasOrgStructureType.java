package ru.sberbank.ditsib.corpclient.database.model;

/**
 * Интерфейс наличия типа оргструктуры.
 */
public interface HasOrgStructureType {

    /**
     * @return текущее значение типа оргструктуры.
     */
    OrgStructureType getOrgStructureType();

    /**
     * @param type новое значение типа оргструктуры.
     */
    void setOrgStructureType(OrgStructureType type);

}
