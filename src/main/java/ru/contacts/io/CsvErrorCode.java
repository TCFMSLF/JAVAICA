package ru.contacts.io;

/**
 * Коды ошибок загрузки CSV (уровень Максимум).
 * Каждая битая строка классифицируется одним кодом и показывается в GUI.
 */
public enum CsvErrorCode {
    /** Неверное число полей (ожидается 7). */
    WRONG_FIELD_COUNT,
    /** Неизвестное значение колонки type (ожидается CONTACT, EMERGENCY или CORPORATE). */
    UNKNOWN_TYPE,
    /** Пустое обязательное поле (имя, телефон или e-mail). */
    EMPTY_REQUIRED_FIELD,
    /** Пустая должность у корпоративного контакта. */
    EMPTY_POSITION
}
