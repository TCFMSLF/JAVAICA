package ru.contacts.io.ser;

import java.io.IOException;

/**
 * Ошибка сериализации/десериализации с кодом для GUI и тестов.
 * Наследник {@link IOException}, поэтому сигнатуры сериализаторов —
 * обычные {@code throws IOException}, как в задании.
 */
public class SerializationException extends IOException {

    /** Коды ошибок формата. */
    public enum Code {
        /** Нет метки версии или она не равна текущей. */
        BAD_VERSION,
        /** Файл новее программы (схема из будущего). */
        FUTURE_VERSION,
        /** Обрезанный/непарсящийся файл. */
        TRUNCATED,
        /** Неизвестное значение поля type. */
        UNKNOWN_TYPE,
        /** Пустое обязательное поле записи. */
        BAD_FIELD
    }

    private final Code code;

    public SerializationException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public SerializationException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
