/**
 * Одна пропущенная (битая) строка CSV-файла.
 *
 * <p>Неизменяемый носитель причины пропуска: номер строки, код ошибки
 * и исходный текст строки. GUI показывает список таких ошибок в диалоге
 * после загрузки, вместо молчаливого пропуска.
 */
public final class CsvRowException extends Exception {

    private final int lineNo;
    private final CsvErrorCode code;
    private final String rawLine;

    public CsvRowException(int lineNo, CsvErrorCode code, String rawLine, String detail) {
        super("Строка " + lineNo + " [" + code + "]: " + detail);
        this.lineNo = lineNo;
        this.code = code;
        this.rawLine = rawLine;
    }

    /** Номер строки в файле (считая с 1, включая заголовок). */
    public int getLineNo() {
        return lineNo;
    }

    /** Код причины пропуска. */
    public CsvErrorCode getCode() {
        return code;
    }

    /** Исходный текст строки. */
    public String getRawLine() {
        return rawLine;
    }
}
