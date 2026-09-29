package ru.sberbank.ditsib.transport.limits.model.limit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PeriodData {
    JANUARY(LimitSharingType.MONTHLY),
    FEBRUARY(LimitSharingType.MONTHLY),
    MARCH(LimitSharingType.MONTHLY),
    APRIL(LimitSharingType.MONTHLY),
    MAY(LimitSharingType.MONTHLY),
    JUNE(LimitSharingType.MONTHLY),
    JULY(LimitSharingType.MONTHLY),
    AUGUST(LimitSharingType.MONTHLY),
    SEPTEMBER(LimitSharingType.MONTHLY),
    OCTOBER(LimitSharingType.MONTHLY),
    NOVEMBER(LimitSharingType.MONTHLY),
    DECEMBER(LimitSharingType.MONTHLY),
    Q1(LimitSharingType.QUARTER),
    Q2(LimitSharingType.QUARTER),
    Q3(LimitSharingType.QUARTER),
    Q4(LimitSharingType.QUARTER),;

    private final LimitSharingType type;
}
