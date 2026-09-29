package ru.sberbank.ditsib.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Пагинация, сортировка, фильтрация.
 *
 * @param <T> тип перечисления, где указаны доступные для фильтрации и сортировки поля.
 */
@Setter
public abstract class PageSortFilterParameters<T extends Enum<?>> {
    
    /**
     * Создать новый объект.
     *
     * @param defaultField поле сортировки по-умолчанию.
     */
    protected PageSortFilterParameters(T defaultField) {
        field = defaultField;
    }
    
    /**
     * Страница выборки.
     */
    @Getter
    @Schema(title = "Страница", description = "Страница выборки", defaultValue = "0")
    private int page = 0;
    
    /**
     * Размер страницы выборки.
     */
    @Getter
    @Schema(title = "Размер страницы", description = "Размер страницы выборки", defaultValue = "20")
    private int size = 20;
    
    /**
     * Направление сортировки выборки.
     */
    @Getter
    @Schema(title = "Направление сортировки", description = "Направление сортировки выборки", defaultValue = "ASC")
    private Direction direction = Direction.ASC;
    
    /**
     * Поле сортировки выборки.
     */
    @Getter
    @Schema(title = "Поле сортировки", description = "Поле сортировки выборки")
    private T field;
    
    /**
     * Получить данные для осуществления фильтрации. За основу берется состав полей перечисления полей для фильтрации, указанного как тип класса
     * фильтра. Объект фильтра заполняется следующим образом: берется значения перечисления фильтра, производится поиск среди полей класса поля с
     * именем перечисления. Если такое поле находится, его строковое значение кладется в фильтр. Если поля нет, значение перечисления пропускается.
     * Важно: в наследнике класса названия полей должны обязательно совпадать со значениями перечисления.
     *
     * @return map фильтров.
     */
    public Map<T, Serializable> getFilter() {
        var map = new HashMap<T, Serializable>();
        //noinspection unchecked
        var filterClass = (Class<T>) field.getClass();
        var constants = filterClass.getEnumConstants();
        if (constants != null) {
            for (T enumItem : constants) {
                try {
                    var fieldName = enumItem.name();
                    if (enumItem instanceof SortField item) {
                        fieldName = item.getName();
                    }
                    var declaredField = getClass().getDeclaredField(fieldName);
                    declaredField.setAccessible(true); // NOSONAR
                    var rawValue = declaredField.get(this);
                    var value = Optional.ofNullable(rawValue).map(Serializable.class::cast).orElse(null);
                    map.put(enumItem, value);
                } catch (NoSuchFieldException | IllegalAccessException ignore) {
                    // ignore error
                }
            }
        }
        return map;
    }
    
}
