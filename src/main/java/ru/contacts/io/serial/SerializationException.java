package ru.contacts.io.serial;

import java.io.IOException;

/**
 * Ошибка чтения/записи файла сериализации с машиночитаемым кодом.
 */
public class SerializationException extends IOException {

    private static final long serialVersionUID = 1L;

    private final SerializationErrorCode code;

    public SerializationException(SerializationErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public SerializationException(SerializationErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public SerializationErrorCode getCode() {
        return code;
    }
}
