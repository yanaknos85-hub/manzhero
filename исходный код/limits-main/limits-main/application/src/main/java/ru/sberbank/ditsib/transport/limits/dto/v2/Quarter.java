package ru.sberbank.ditsib.transport.limits.dto.v2;

public enum Quarter implements Period {
    Q1,
    Q2,
    Q3,
    Q4;

    @Override
    public ru.sberbank.ditsib.transport.limits.model.limit.Period toModel() {
        return ru.sberbank.ditsib.transport.limits.model.limit.Quarter.valueOf(name());
    }
}
