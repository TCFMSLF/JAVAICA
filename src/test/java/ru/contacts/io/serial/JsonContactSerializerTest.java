package ru.contacts.io.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;

class JsonContactSerializerTest {

    @TempDir
    Path tempDir;

    private final Serializer<Contact[]> serializer = new JsonContactSerializer();

    @Test
    void roundTripPreservesAllContactTypesAndFields() throws Exception {
        Contact[] source = TestContacts.sample();
        Path file = tempDir.resolve("contacts.json");

        serializer.save(source, file);
        Contact[] loaded = serializer.load(file);

        TestContacts.assertSameContacts(source, loaded);
    }

    @Test
    void loadsPreparedJsonFile() throws Exception {
        Contact[] loaded = serializer.load(resource("/contacts.json"));

        assertEquals(3, loaded.length);
        assertEquals(Contact.class, loaded[0].getClass());
        assertEquals("Иван Петров", loaded[0].getName());
        assertEquals(CorporateContact.class, loaded[2].getClass());
        assertEquals("Менеджер", ((CorporateContact) loaded[2]).getPosition());
    }

    @Test
    void rejectsBrokenJsonFile() throws Exception {
        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.load(resource("/contacts-broken.json")));

        assertEquals(SerializationErrorCode.BROKEN_FILE, error.getCode());
    }

    @Test
    void roundTripsEmptyContactArray() throws Exception {
        Path file = tempDir.resolve("empty.json");

        serializer.save(new Contact[0], file);
        Contact[] loaded = serializer.load(file);

        assertEquals(0, loaded.length);
    }

    @Test
    void rejectsInvalidContactData() throws Exception {
        Path file = tempDir.resolve("invalid.json");
        Files.writeString(file, """
                {
                  "schemaVersion": 1,
                  "contacts": [ {
                    "type": "CONTACT",
                    "name": "",
                    "phone": "+7-900-000-00-00",
                    "email": "no-at-sign",
                    "organization": "Организация"
                  } ]
                }
                """);

        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.load(file));

        assertEquals(SerializationErrorCode.INVALID_CONTACT_DATA, error.getCode());
    }

    @Test
    void rejectsInvalidContactBeforeSaving() {
        Contact invalid = new Contact("", "+7-900-000-00-00", "no-at-sign", "Организация");
        Path file = tempDir.resolve("invalid-out.json");

        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.save(new Contact[]{invalid}, file));

        assertEquals(SerializationErrorCode.INVALID_CONTACT_DATA, error.getCode());
    }

    @Test
    void rejectsUnsupportedSchemaVersion() throws Exception {
        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.load(resource("/contacts-v2.json")));

        assertEquals(SerializationErrorCode.UNSUPPORTED_VERSION, error.getCode());
    }

    private static Path resource(String name) throws Exception {
        return Path.of(JsonContactSerializerTest.class.getResource(name).toURI());
    }
}
