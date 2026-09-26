package ru.contacts.io;

import java.util.Collections;
import java.util.List;

import ru.contacts.model.Contact;

/**
 * Результат загрузки CSV: корректные контакты и список ошибок по битым строкам.
 * Битые строки пропускаются, но каждая фиксируется с номером и кодом.
 */
public class CsvLoadResult {
    private final List<Contact> contacts;
    private final List<CsvRowError> errors;

    public CsvLoadResult(List<Contact> contacts, List<CsvRowError> errors) {
        this.contacts = Collections.unmodifiableList(contacts);
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public List<CsvRowError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
