package ru.sber.transport.fraud.monitoring.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Интерфейс данных кейса фрода с email-перепиской
 */
public interface FraudCaseData {

    /**
     * Идентификатор кейса фрода
     *
     * @return идентификатор кейса фрода
     */
    UUID getId();

    /**
     * Вердикт ИИ
     *
     * @return вердикт ИИ
     */
    String getAiVerdict();

    /**
     * Комментарий от ИИ
     *
     * @return комментарий от ИИ
     */
    String getAiComment();

    /**
     * Комментарий по нарушению
     *
     * @return комментарий по нарушению
     */
    String getComment();

    /**
     * Флаг необходимости валидации
     *
     * @return true если требуется валидация
     */
    boolean isNeedValidation();

    /**
     * Список email-сообщений
     *
     * @return список email-сообщений
     */
    List<? extends FraudMessageItem> getMessaging();

    /**
     * Email-сообщение
     */
    interface FraudMessageItem {

        /**
         * Дата/время отправки письма
         *
         * @return дата/время отправки письма
         */
        LocalDateTime getMessageDate();

        /**
         * Отправитель
         *
         * @return отправитель
         */
        String getFromEmail();

        /**
         * Получатель
         *
         * @return получатель
         */
        String getToEmail();

        /**
         * Тело письма
         *
         * @return тело письма
         */
        String getBody();
    }
}