package ru.sber.transport.utils;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;

@AllArgsConstructor
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

    private T object;

    @Override
    public @NonNull T getObject(@NonNull Object... args) throws BeansException {
        return object;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return object;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return object;
    }

    @Override
    public @NonNull T getObject() throws BeansException {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }
}
