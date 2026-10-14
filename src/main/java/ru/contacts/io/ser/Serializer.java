package ru.contacts.io.ser;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Общий контракт сериализации (Лаб. 3).
 * GUI и тесты работают через этот интерфейс, не зная,
 * что внутри: JSON, бинарный формат или что-то ещё.
 */
public interface Serializer {

    /** Короткое имя формата: "json", "bin". */
    String format();

    /** Сохранить данные в файл. */
    void save(ContactData data, Path file) throws IOException;

    /** Загрузить данные из файла. */
    ContactData load(Path file) throws IOException;
}
