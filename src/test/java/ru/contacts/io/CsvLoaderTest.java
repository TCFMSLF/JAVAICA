package ru.contacts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

class CsvLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsPreparedCsvAndReportsBrokenRows() throws Exception {
        CsvLoadResult result = CsvLoader.load(resource("/contacts.csv"));

        assertEquals(4, result.getContacts().size());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.hasErrors());
        assertTrue(result.getContacts().get(1) instanceof EmergencyContact);
        assertTrue(result.getContacts().get(2) instanceof CorporateContact);
    }

    @Test
    void savesAndReloadsContactsWithoutErrors() throws Exception {
        List<Contact> source = List.of(
                new Contact("Иван Петров", "+7-900-111-22-33", "ivan@example.com", "ООО Ромашка"),
                new EmergencyContact("Мама", "+7-900-000-00-01", "mama@example.com", ""),
                new CorporateContact("Анна Сидорова", "+7-900-222-33-44", "anna@corp.ru",
                        "ООО Ромашка", "Менеджер", "1234"));
        Path file = tempDir.resolve("contacts.csv");

        CsvLoader.save(source, file);
        CsvLoadResult result = CsvLoader.load(file);

        assertEquals(3, result.getContacts().size());
        assertFalse(result.hasErrors());
        assertEquals(CorporateContact.class, result.getContacts().get(2).getClass());
    }

    private static Path resource(String name) throws Exception {
        return Path.of(CsvLoaderTest.class.getResource(name).toURI());
    }
}
