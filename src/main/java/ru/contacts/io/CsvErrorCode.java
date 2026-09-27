package ru.contacts.io;

// Коды ошибок загрузки CSV (уровень Максимум). Каждая битая строка получает ровно
// один код, который показывается в GUI вместе с номером строки.
public enum CsvErrorCode {
    NOT_A_FILE,
    BAD_HEADER,
    // ожидается 7 полей — столько в заголовке
    WRONG_FIELD_COUNT,
    // ожидается одно из: CONTACT, EMERGENCY, CORPORATE
    UNKNOWN_TYPE,
    EMPTY_REQUIRED_FIELD,
    EMPTY_POSITION
}
