package ru.contacts.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
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

// Загрузка/сохранение контактов в CSV с разделителем ';'.
//
// Формат: type;name;phone;email;organization;position;internalNumber
// type = CONTACT | EMERGENCY | CORPORATE
//
// Два разных механизма сообщения об ошибках, потому что ошибки разные:
//   CsvRowError    — битая строка. Ожидаемый исход разбора: строка пропускается,
//                    файл читается дальше, все ошибки собираются в список и
//                    показываются в GUI одним диалогом.
//   CsvLoadException — файл целиком непригоден. Возвращать нечего, поэтому
//                    это исключение с кодом ошибки; GUI ловит его отдельно.

public final class CsvLoader {

    private static final String DELIMITER = ";";
    private static final String HEADER =
            String.join(DELIMITER, "type", "name", "phone", "email",
                    "organization", "position", "internalNumber");
    private static final int FIELD_COUNT = 7;
    private static final String BOM = "\uFEFF";
    // Задана явно, а не через writer.newLine(): newLine() пишет разделитель
    // текущей ОС, и файл перестаёт открываться одинаково в Windows и Linux.
    private static final String NEW_LINE = "\n";

    private CsvLoader() {
    }

    // Читает файл и разбирает его построчно.
    //
    // Битая строка не прерывает разбор: попадает в CsvLoadResult с кодом
    // CsvErrorCode и пропускается. Если файл целиком непригоден — бросается
    // CsvLoadException, возвращать в таком случае нечего.
    public static CsvLoadResult load(Path file) throws IOException {
        // Проверяем заранее: на Windows Files.readAllLines на папке бросает IOException
        // с невнятным «Is a directory».
        if (Files.isDirectory(file)) {
            throw new CsvLoadException(CsvErrorCode.NOT_A_FILE, "это папка, а не файл: " + file);
        }

        List<Contact> contacts = new ArrayList<>();
        List<CsvRowError> errors = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException ex) {
            // Файл читается, но не в UTF-8: причина в данных, а не в доступе —
            // сообщение стандартного исключения («Input length = 1») пользователю ничего не объясняет.
            throw new CsvLoadException(CsvErrorCode.BAD_HEADER,
                    "файл не в кодировке UTF-8: " + file, ex);
        }

        if (lines.isEmpty()) {
            throw new CsvLoadException(CsvErrorCode.BAD_HEADER,
                    "файл пуст, ожидалась строка заголовка: " + HEADER);
        }

        if (!stripBom(lines.get(0)).trim().equals(HEADER)) {
            // Терпимо: неизвестная схема не мешает разобрать строки с ожидаемым
            // числом полей, поэтому фиксируем ошибку и продолжаем чтение.
            errors.add(new CsvRowError(1, CsvErrorCode.BAD_HEADER,
                    "ожидался заголовок: " + HEADER));
        }

        for (int i = 1; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                // Пустые строки пропускаем молча: это не ошибка данных.
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

    static ParseOutcome parseLine(String line, int lineNumber) {
        // -1 обязателен: без него split отбрасывает хвостовые пустые поля, и строка
        // "CONTACT;Иван;123;a@b;Org;;" даст 3 поля вместо 7 — ложный WRONG_FIELD_COUNT.
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
                    // Имя константы enum совпадает с тегом в CSV, поэтому
                    // выводим список без преобразования.
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

    // Заполнено ровно одно: contact у success, error у failure.
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
