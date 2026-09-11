import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Загрузка контактов из CSV.
 *
 * <p>Формат файла (разделитель {@code ;}, кодировка UTF-8):
 * <pre>
 * type;name;phone;email;organization;position;internalNumber
 * contact;Анна Смирнова;+7-900-111-22-33;anna.smirnova@example.com;ООО Ромашка;;
 * corporate;Антон Смирнов;+7-495-123-45-67;a.smirnov@vector.ru;АО Вектор;Менеджер;101
 * emergency;Иван Смирнов (SOS);+7-900-000-00-01;sos.smirnov@example.com;—;;
 * </pre>
 * <ul>
 *   <li>первая строка — заголовок, пропускается;</li>
 *   <li>{@code type}: {@code contact} / {@code emergency} / {@code corporate} (по умолчанию contact);</li>
 *   <li>битые строки пропускаются, каждая — с кодом {@link CsvErrorCode}
 *       в {@link CsvLoadResult#errors()};</li>
 *   <li>ошибки чтения файла пробрасываются вызывающему ({@code IOException}),
 *       GUI показывает их диалогом.</li>
 * </ul>
 */
public final class CsvLoader {

    /** читает файл построчно, разбирает каждую строку и собирает сущности + ошибки */
    public CsvLoadResult load(Path file) throws IOException {
        List<Contact> result = new ArrayList<>();
        List<CsvRowException> errors = new ArrayList<>();
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();              // строка-заголовок
            if (header == null) {
                return new CsvLoadResult(result, errors);
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    result.add(parseLine(line, lineNo));
                } catch (CsvRowException e) {
                    errors.add(e);
                }
            }
        }
        return new CsvLoadResult(result, errors);
    }

    /**
     * Разбор одной строки CSV в объект.
     *
     * @throws CsvRowException с кодом причины, если строка битая
     */
    private Contact parseLine(String line, int lineNo) throws CsvRowException {
        String[] parts = line.split(";", -1);
        if (parts.length != 7) {
            throw new CsvRowException(lineNo, CsvErrorCode.WRONG_FIELD_COUNT, line,
                    "ожидалось 7 колонок, получено " + parts.length);
        }
        String type = parts[0].trim().toLowerCase();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String organization = parts[4].trim();
        String position = parts[5].trim();
        String internalNumber = parts[6].trim();

        // Обязательные поля
        if (name.isEmpty()) {
            throw new CsvRowException(lineNo, CsvErrorCode.EMPTY_NAME, line,
                    "имя не должно быть пустым");
        }
        if (phone.isEmpty()) {
            throw new CsvRowException(lineNo, CsvErrorCode.EMPTY_PHONE, line,
                    "телефон не должен быть пустым");
        }
        if (!email.isBlank() && !email.contains("@")) {
            throw new CsvRowException(lineNo, CsvErrorCode.INVALID_EMAIL, line,
                    "e-mail должен содержать '@'");
        }

        switch (type) {
            case "corporate": {
                CorporateContact cc = new CorporateContact(name, phone, email, organization,
                        position, internalNumber);
                List<String> validation = cc.validate();
                if (!validation.isEmpty()) {
                    throw new CsvRowException(lineNo, CsvErrorCode.INVALID_CORPORATE_FIELDS, line,
                            String.join("; ", validation));
                }
                return cc;
            }
            case "emergency":
                return new EmergencyContact(name, phone, email, organization);
            case "contact":
            case "":
                return new Contact(name, phone, email, organization);
            default:
                throw new CsvRowException(lineNo, CsvErrorCode.UNKNOWN_TYPE, line,
                        "неизвестный тип: " + parts[0].trim());
        }
    }
}
