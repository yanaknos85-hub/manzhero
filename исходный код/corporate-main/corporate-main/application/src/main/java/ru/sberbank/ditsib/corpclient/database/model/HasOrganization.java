package ru.sberbank.ditsib.corpclient.database.model;

/**
 * Интерфейс наличия связки с организацией.
 */
public interface HasOrganization {

    /**
     * @return закрепленная организация.
     */
    Organization getOrganization();

    /**
     * @param organization организация для закрепления.
     */
    void setOrganization(Organization organization);

}
