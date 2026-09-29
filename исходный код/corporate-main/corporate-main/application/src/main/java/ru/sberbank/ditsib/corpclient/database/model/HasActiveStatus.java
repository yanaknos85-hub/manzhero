package ru.sberbank.ditsib.corpclient.database.model;

public interface HasActiveStatus {

    ActiveStatus getActiveStatus();

    void setActiveStatus(ActiveStatus status);

}
