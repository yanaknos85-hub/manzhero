package ru.sberbank.ditsib.transport.limits.model.limit;

/**
 * Интерфейс для объектов с периодом
 */
public interface HasPeriod {

    PeriodData getPeriodData();

    void setPeriodData(PeriodData period);

    default <T extends Period> T getPeriod() {
        var periodData = getPeriodData();
        return (T) switch (periodData.getType()) {
            case MONTHLY, PERCENTS -> Month.valueOf(periodData.name());
            case QUARTER -> Quarter.valueOf(periodData.name());
        };
    }

    default <T extends Period> void setPeriod(T period) {
        setPeriodData(PeriodData.valueOf(period.name()));
    }

}
