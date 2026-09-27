package ru.contacts.io;

// Одна битая строка CSV: номер, код ошибки и пояснение. Не исключение, потому что
// битых строк бывает много и они не прерывают разбор.
public class CsvRowError {
    // Нумерация с 1 и с учётом строки заголовка — как её видно в редакторе.
    private final int lineNumber;
    private final CsvErrorCode code;
    private final String message;

    public CsvRowError(int lineNumber, CsvErrorCode code, String message) {
        this.lineNumber = lineNumber;
        this.code = code;
        this.message = message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public CsvErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "строка " + lineNumber + ": " + code + " — " + message;
    }
}
