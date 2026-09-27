package ru.contacts.io;

import java.util.List;

import ru.contacts.model.Contact;

// Итог загрузки CSV: корректные контакты и список ошибок по битым строкам.
// Исключения здесь не бывает — оно живёт в CsvLoadException.
public class CsvLoadResult {
    // List.copyOf, а не unmodifiableList: нужна копия, иначе вызывающий продолжит
    // менять списки, которые мы отдали.
    private final List<Contact> contacts;
    private final List<CsvRowError> errors;

    public CsvLoadResult(List<Contact> contacts, List<CsvRowError> errors) {
        this.contacts = List.copyOf(contacts);
        this.errors = List.copyOf(errors);
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
