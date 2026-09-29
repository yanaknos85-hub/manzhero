package ru.sberbank.ditsib.transport.limits.model.limit;

public enum Quarter implements Period {
    Q1,
    Q2,
    Q3,
    Q4;
    
    public static Quarter valueOf(int source) {
        return Quarter.values()[source - 1];
    }
    
}
