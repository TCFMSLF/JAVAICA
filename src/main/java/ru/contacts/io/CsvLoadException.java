package ru.contacts.io;

import java.io.IOException;

// Файл непригоден целиком: разбирать нечего, поэтому это исключение, а не запись
// в списке ошибок строк. Битая строка — ожидаемый исход разбора, её пропускают и читают
// файл дальше, поэтому ошибка строки лежит в CsvLoadResult.
//
// extends IOException, а не RuntimeException: загрузка файла — это ввод-вывод,
// вызывающий может поймать оба случая одним блоком, а этот — отдельно,
// чтобы показать код ошибки.
public class CsvLoadException extends IOException {

    // IOException реализует Serializable, поэтому версию класса фиксируем явно.
    private static final long serialVersionUID = 1L;

    private final CsvErrorCode code;

    public CsvLoadException(CsvErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public CsvLoadException(CsvErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public CsvErrorCode getCode() {
        return code;
    }
}
