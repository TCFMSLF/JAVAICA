package ru.contacts.io.serial;

/**
 * Коды ошибок сериализации/десериализации.
 */
public enum SerializationErrorCode {
    /** Файл повреждён или имеет неверный формат. */
    BROKEN_FILE,
    /** Версия схемы файла не поддерживается текущей версией приложения. */
    UNSUPPORTED_VERSION,
    /** Данные контакта некорректны (пустые обязательные поля и т. п.). */
    INVALID_CONTACT_DATA
}
