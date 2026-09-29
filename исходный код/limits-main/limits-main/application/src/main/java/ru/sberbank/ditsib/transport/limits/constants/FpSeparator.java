package ru.sberbank.ditsib.transport.limits.constants;

/**
 * Possible variants of separate in csv
 */
public enum FpSeparator {
    COMMA(","),
    DOT(".");
    
    private final String separator;
    
    FpSeparator(String separator) {
        this.separator = separator;
    }
    
    public String getSeparator() {
        return separator;
    }
}
