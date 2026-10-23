package ru.contacts.io.serial;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Общий контракт сериализации (Лаб. 3).
 * Позволяет GUI и тестам работать с JSON и бинарным форматом
 * через один интерфейс, не завися от конкретной реализации.
 *
 * @param <T> тип сохраняемых данных
 */
public interface Serializer<T> {

    /** Короткое имя формата: {@code json} или {@code bin}. */
    String format();

    void save(T data, Path file) throws IOException;

    T load(Path file) throws IOException;
}
