package ru.sberbank.ditsib.transport.limits.model.limit;

public interface Period {
    
    @SuppressWarnings("unchecked")
    static <T extends Period> T create(String source) {
        if (source == null) {
            return null;
        }
        if (source.startsWith("Q")) {
            return (T) Quarter.valueOf(source);
        }
        return (T) Month.valueOf(source);
    }

    int ordinal();
    
    String name();
    
    default int getValue() {
        return ordinal() + 1;
    }
    
    default <T extends Period> T next() {
        var newOrdinal = ordinal() + 1;
        //noinspection unchecked
        return (T) getClass().getEnumConstants()[newOrdinal];
    }
    
}
