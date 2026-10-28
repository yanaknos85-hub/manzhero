package ru.sberbank.ditsib.transport.sudir.sudir.providers;

/**
 * Провайдер допустимых URL.
 */
public interface AllowUrlProvider {

    /**
     * Проверить допустимость реферера.
     *
     * @param url URL для проверки.
     * @return <code>true</code> если URL разрешено использовать для перенаправления.
     */
    boolean allow(String url);

}
