package ru.contacts.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Загрузка/сохранение контактов в CSV с разделителем ';'.
 * Битые строки при загрузке пропускаются, каждая фиксируется
 * в {@link CsvLoadResult} с номером строки и кодом {@link CsvErrorCode}.
 *
 * Формат: type;name;phone;email;organization;position;internalNumber
 * type = CONTACT | EMERGENCY | CORPORATE
 */
public final class CsvLoader {

    private static final String HEADER = "type;name;phone;email;organization;position;internalNumber";
    private static final int FIELD_COUNT = 7;

    private CsvLoader() {
    }

    public static CsvLoadResult load(Path file) throws IOException {
        List<Contact> contacts = new ArrayList<>();
        List<CsvRowError> errors = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (i == 0) {
                continue; // строка-заголовок
            }
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
        String[] parts = line.split(";", -1);
        if (parts.length != FIELD_COUNT) {
            return ParseOutcome.failure(lineNumber, CsvErrorCode.WRONG_FIELD_COUNT,
                    "ожидалось полей: " + FIELD_COUNT + ", найдено: " + parts.length);
        }
        String type = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String organization = parts[4].trim();
        String position = parts[5].trim();
        String internalNumber = parts[6].trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            return ParseOutcome.failure(lineNumber, CsvErrorCode.EMPTY_REQUIRED_FIELD,
                    "имя, телефон и e-mail обязательны");
        }

        switch (type) {
            case "CONTACT":
                return ParseOutcome.success(new Contact(name, phone, email, organization));
            case "EMERGENCY":
                return ParseOutcome.success(new EmergencyContact(name, phone, email, organization));
            case "CORPORATE":
                if (position.isEmpty()) {
                    return ParseOutcome.failure(lineNumber, CsvErrorCode.EMPTY_POSITION,
                            "у корпоративного контакта обязательна должность");
                }
                return ParseOutcome.success(
                        new CorporateContact(name, phone, email, organization, position, internalNumber));
            default:
                return ParseOutcome.failure(lineNumber, CsvErrorCode.UNKNOWN_TYPE,
                        "неизвестный тип: '" + type + "'");
        }
    }

    public static void save(List<Contact> contacts, Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Contact c : contacts) {
                writer.write(toLine(c));
                writer.newLine();
            }
        }
    }

    static String toLine(Contact c) {
        if (c instanceof CorporateContact) {
            CorporateContact cc = (CorporateContact) c;
            return String.join(";",
                    "CORPORATE", c.getName(), c.getPhone(), c.getEmail(),
                    c.getOrganization(), cc.getPosition(), cc.getInternalNumber());
        }
        if (c instanceof EmergencyContact) {
            return String.join(";",
                    "EMERGENCY", c.getName(), c.getPhone(), c.getEmail(),
                    c.getOrganization(), "", "");
        }
        return String.join(";",
                "CONTACT", c.getName(), c.getPhone(), c.getEmail(),
                c.getOrganization(), "", "");
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
