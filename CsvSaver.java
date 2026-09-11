import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Сохранение контактов в CSV (парное к {@link CsvLoader}).
 *
 * <p>Формат полностью совпадает с форматом загрузки:
 * разделитель {@code ;}, кодировка UTF-8, первая строка — заголовок:
 * <pre>
 * type;name;phone;email;organization;position;internalNumber
 * </pre>
 * Тип определяется по классу объекта:
 * {@code emergency} / {@code corporate} / {@code contact}.
 */
public final class CsvSaver {

    private CsvSaver() {
    }

    /** Сохраняет список контактов в файл. */
    public static void save(List<Contact> contacts, Path file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("type;name;phone;email;organization;position;internalNumber");
            w.newLine();
            for (Contact c : contacts) {
                w.write(toLine(c));
                w.newLine();
            }
        }
    }

    private static String toLine(Contact c) {
        String type;
        String position = "";
        String internalNumber = "";
        if (c instanceof EmergencyContact) {
            type = "emergency";
        } else if (c instanceof CorporateContact cc) {
            type = "corporate";
            position = nn(cc.getPosition());
            internalNumber = nn(cc.getInternalNumber());
        } else {
            type = "contact";
        }
        return String.join(";",
                type,
                nn(c.getName()),
                nn(c.getPhone()),
                nn(c.getEmail()),
                nn(c.getOrganization()),
                position,
                internalNumber);
    }

    private static String nn(String s) {
        if (s == null) {
            return "";
        }
        // Разделитель ';' и переводы строк ломают формат — заменяем на пробел.
        return s.replace(";", " ").replace("\r", " ").replace("\n", " ");
    }
}
