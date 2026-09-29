package ru.sberbank.ditsib.transport.limits.constants;

/**
 * Possible variants of separate in csv
 */
public enum CsvSeparator {
    COMMA(","),
    SEMICOLON(";"),
    WHITESPACE(" "),
    TAB("\t"),
    OTHER("");
    
    private final String separator;
    
    CsvSeparator(String separator) {
        this.separator = separator;
    }
    
    public String getSeparator() {
        return separator;
    }
}
