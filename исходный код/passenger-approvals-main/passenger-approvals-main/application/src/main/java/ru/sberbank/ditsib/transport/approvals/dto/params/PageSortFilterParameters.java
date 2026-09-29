package ru.sberbank.ditsib.transport.approvals.dto.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//TODO аналогичный механизм реализован в сервисе corp-client. Перенести в один общий модуль
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
    public PageSortFilterParameters(T defaultField) {
        field = defaultField;
    }
    
    /**
     * Направление сортировки выборки.
     */
    @Getter
    @Schema(title = "Направление сортировки", description = "Направление сортировки выборки", defaultValue = "ASC")
    private Sort.Direction direction = Sort.Direction.ASC;
    
    /**
     * Поле сортировки выборки.
     */
    @Getter
    @Schema(title = "Поле сортировки", description = "Поле сортировки выборки")
    private T field;
    
    public abstract boolean isFieldsEmpty();
    
    /**
     * Получить данные для осуществления фильтрации. За основу берется состав полей перечисления полей для фильтрации, указанного как тип класса
     * фильтра. Объект фильтра заполняется следующим образом: берется значения перечисления фильтра, производится поиск среди полей класса поля с
     * именем перечисления. Если такое поле находится, его строковое значение кладется в фильтр. Если поля нет, значение перечисления пропускается.
     * Важно: в наследнике класса названия полей должны обязательно совпадать со значениями перечисления.
     */
    public Map<T, String> getFilter() {
        var map = new HashMap<T, String>();
        //noinspection unchecked
        var filterClass = (Class<T>) field.getClass();
        var constants = filterClass.getEnumConstants();
        if (constants != null) {
            for (T enumItem : constants) {
                try {
                    var fieldName = enumItem.name();
                    if (enumItem instanceof SortField) {
                        fieldName = ((SortField) enumItem).getName();
                    }
                    var field = getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    var rawValue = field.get(this);
                    var value = Optional.ofNullable(rawValue).map(String::valueOf).orElse(null);
                    map.put(enumItem, value);
                } catch (NoSuchFieldException | IllegalAccessException ignore) {
                }
            }
        }
        return map;
    }
    
}

