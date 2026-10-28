package ru.sber.transport.trip.registries.database.model;

/**
 * Доступные типы согласований.
 */
public enum RegistryState {

    INIT,
    
    REGISTRY_SENT_SUCCESS,

    REGISTRY_SENDING_FAILED,
    
    ACT_NUMBER_RECIEVED_SUCCESS,

    ACT_NUMBER_RECIEVING_FAILED,

    REGISTRY_PROCESSED_FINISHED,

    REGISTRY_PROCESSING_FAILED
}
