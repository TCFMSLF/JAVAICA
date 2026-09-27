package ru.contacts.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ru.contacts.model.Contact;
import ru.contacts.model.ContactType;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Загрузка/сохранение контактов в CSV с разделителем ';'.
 * Битые строки при загрузке пропускаются, каждая фиксируется
 * в {@link CsvLoadResult} с номером строки и кодом {@link CsvErrorCode}.
 *
 * <p>Формат: type;name;phone;email;organization;position;internalNumber
 * type = CONTACT | EMERGENCY | CORPORATE
 *
 * <p>Файл сохраняется в UTF-8 <b>с BOM</b> и с переводом строки {@code \n} независимо от ОС:
 * без BOM кириллица в Excel открывается как кракозябры, а платформенный разделитель строк
 * делает файл непереносимым между Windows и Linux.
 *
 * <p>Экранирование значений не поддерживается: разделитель {@code ';'} в имени или телефоне
 * приведёт к {@link CsvErrorCode#WRONG_FIELD_COUNT} при чтении. Для форматов с экранированием
 * в лабораторной № 3 используется Jackson.
 */
public final class CsvLoader {

    private static final String DELIMITER = ";";
    private static final String HEADER =
            String.join(DELIMITER, "type", "name", "phone", "email",
                    "organization", "position", "internalNumber");
    private static final int FIELD_COUNT = 7;
    private static final String BOM = "\uFEFF";
    private static final String NEW_LINE = "\n";

    private CsvLoader() {
    }

    public static CsvLoadResult load(Path file) throws IOException {
        List<Contact> contacts = new ArrayList<>();
        List<CsvRowError> errors = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        if (lines.isEmpty()) {
            errors.add(new CsvRowError(1, CsvErrorCode.BAD_HEADER,
                    "файл пуст, ожидалась строка заголовка: " + HEADER));
            return new CsvLoadResult(contacts, errors);
        }

        if (!stripBom(lines.get(0)).trim().equals(HEADER)) {
            errors.add(new CsvRowError(1, CsvErrorCode.BAD_HEADER,
                    "ожидался заголовок: " + HEADER));
        }

        for (int i = 1; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            ParseOutcome outcome = parseLine(line, lineNumber);
            if (outcome.contact() != null) {
                contacts.add(outcome.contact());
            } else {
                errors.add(outcome.error());
            }
        }
        return new CsvLoadResult(contacts, errors);
    }

    /**
     * Разбирает одну строку CSV.
     *
     * @return контакт либо ошибку с кодом, если строка битая
     */
    static ParseOutcome parseLine(String line, int lineNumber) {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != FIELD_COUNT) {
            return ParseOutcome.failure(lineNumber, CsvErrorCode.WRONG_FIELD_COUNT,
                    "ожидалось полей: " + FIELD_COUNT + ", найдено: " + parts.length);
        }
        String tag = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String organization = parts[4].trim();
        String position = parts[5].trim();
        String internalNumber = parts[6].trim();

        Optional<ContactType> type = ContactType.fromTag(tag);
        if (type.isEmpty()) {
            return ParseOutcome.failure(lineNumber, CsvErrorCode.UNKNOWN_TYPE,
                    "неизвестный тип: '" + tag + "'; ожидается один из: "
                            + Arrays.toString(ContactType.values()));
        }

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            return ParseOutcome.failure(lineNumber, CsvErrorCode.EMPTY_REQUIRED_FIELD,
                    "имя, телефон и e-mail обязательны");
        }

        if (type.get() == ContactType.CORPORATE) {
            if (position.isEmpty()) {
                return ParseOutcome.failure(lineNumber, CsvErrorCode.EMPTY_POSITION,
                        "у корпоративного контакта обязательна должность");
            }
            return ParseOutcome.success(
                    new CorporateContact(name, phone, email, organization, position, internalNumber));
        }

        if (type.get() == ContactType.EMERGENCY) {
            return ParseOutcome.success(new EmergencyContact(name, phone, email, organization));
        }
        return ParseOutcome.success(new Contact(name, phone, email, organization));
    }

    public static void save(List<Contact> contacts, Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(BOM);
            writer.write(HEADER);
            writer.write(NEW_LINE);
            for (Contact c : contacts) {
                writer.write(toLine(c));
                writer.write(NEW_LINE);
            }
        }
    }

    static String toLine(Contact c) {
        String position = "";
        String internalNumber = "";
        if (c instanceof CorporateContact corporate) {
            position = orEmpty(corporate.getPosition());
            internalNumber = orEmpty(corporate.getInternalNumber());
        }
        return String.join(DELIMITER,
                ContactType.of(c).tag(), c.getName(), c.getPhone(), c.getEmail(),
                c.getOrganization(), position, internalNumber);
    }

    private static String stripBom(String line) {
        return line.startsWith(BOM) ? line.substring(BOM.length()) : line;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Внутренний итог разбора одной строки: либо контакт, либо ошибка. */
    static final class ParseOutcome {
        private final Contact contact;
        private final CsvRowError error;

        private ParseOutcome(Contact contact, CsvRowError error) {
            this.contact = contact;
            this.error = error;
        }

        static ParseOutcome success(Contact contact) {
            return new ParseOutcome(contact, null);
        }

        static ParseOutcome failure(int lineNumber, CsvErrorCode code, String message) {
            return new ParseOutcome(null, new CsvRowError(lineNumber, code, message));
        }

        Contact contact() {
            return contact;
        }

        CsvRowError error() {
            return error;
        }
    }
}
