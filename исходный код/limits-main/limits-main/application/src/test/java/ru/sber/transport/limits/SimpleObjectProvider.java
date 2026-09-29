package ru.sber.transport.limits;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

@RequiredArgsConstructor
@Getter
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

    private final T object;

    @NotNull
    @Override
    public T getObject(@NotNull Object... args) throws BeansException {
        return this.object;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return this.object;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return this.object;
    }

}
