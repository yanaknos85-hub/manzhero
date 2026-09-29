package ru.sberbank.ditsib.transport.limits.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Статусы заявок на лимит
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LimitRequestStatus {
    
    INIT("На согласовании"),
    DONE_FULLY("Исполнено целиком"),
    DONE_PARTLY("Исполнено частично"),
    CANCELLED("Отменено");
    
    public static final int CANCELLED_BY_USER = 201;
    public static final int CANCELLED_BY_APPROVERS = 202;
    public static final int CANCELLED_BY_DEADLINE = 203;
    
    private final String description;
    
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum LimitStatusCode {
        LIMIT_DECLINED_BY_USER(CANCELLED_BY_USER, "Не согласовано пользователем", CANCELLED),
        LIMIT_DECLINED_BY_EXPIRATION_TIME(CANCELLED_BY_DEADLINE, "Не согласовано по истечению срока", CANCELLED);
        
        private final int code;
        private final String description;
        private final LimitRequestStatus limitRequestStatus;
    }
}
