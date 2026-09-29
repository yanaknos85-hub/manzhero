package ru.sberbank.ditsib.transport.limits.dto.v2;

public enum Month implements Period {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    @Override
    public ru.sberbank.ditsib.transport.limits.model.limit.Period toModel() {
        return ru.sberbank.ditsib.transport.limits.model.limit.Month.valueOf(name());
    }
}
