package ru.sber.transport.corporate.business.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Бизнес-модель цели поездки
 */
@Getter
@Setter
public class TripPurpose {

    /**
     * Идентификатор цели поездки.
     */
    private UUID id;

    /**
     * Наименование цели поездки.
     */
    private String label;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripPurpose that = (TripPurpose) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
