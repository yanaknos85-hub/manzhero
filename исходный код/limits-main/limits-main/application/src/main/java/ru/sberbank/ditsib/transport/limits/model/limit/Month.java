package ru.sberbank.ditsib.transport.limits.model.limit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Month implements Period {
    JANUARY(LimitSharingPercents::getJanuary, LimitSharingPercents::setJanuary),
    FEBRUARY(LimitSharingPercents::getFebruary, LimitSharingPercents::setFebruary),
    MARCH(LimitSharingPercents::getMarch, LimitSharingPercents::setMarch),
    APRIL(LimitSharingPercents::getApril, LimitSharingPercents::setApril),
    MAY(LimitSharingPercents::getMay, LimitSharingPercents::setMay),
    JUNE(LimitSharingPercents::getJune, LimitSharingPercents::setJune),
    JULY(LimitSharingPercents::getJuly, LimitSharingPercents::setJuly),
    AUGUST(LimitSharingPercents::getAugust, LimitSharingPercents::setAugust),
    SEPTEMBER(LimitSharingPercents::getSeptember, LimitSharingPercents::setSeptember),
    OCTOBER(LimitSharingPercents::getOctober, LimitSharingPercents::setOctober),
    NOVEMBER(LimitSharingPercents::getNovember, LimitSharingPercents::setNovember),
    DECEMBER(LimitSharingPercents::getDecember, LimitSharingPercents::setDecember);

    private final Function<LimitSharingPercents, Integer> percentsFunction;

    private final BiConsumer<LimitSharingPercents, Integer> percentsConsumer;
    
    public static Month valueOf(int source) {
        return Month.values()[source - 1];
    }
    
    public static Month valueOf(java.time.Month source) {
        return Month.valueOf(source.getValue());
    }
}
