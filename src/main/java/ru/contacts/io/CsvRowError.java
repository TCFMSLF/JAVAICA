package ru.contacts.io;

/**
 * Описание одной битой строки CSV: номер, код ошибки и пояснение.
 */
public class CsvRowError {
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
