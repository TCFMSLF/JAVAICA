package ru.contacts.io.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;

class BinaryContactSerializerTest {

    @TempDir
    Path tempDir;

    private final Serializer<Contact[]> serializer = new BinaryContactSerializer();

    @Test
    void roundTripPreservesAllContactTypesAndFields() throws Exception {
        Contact[] source = TestContacts.sample();
        Path file = tempDir.resolve("contacts.bin");

        serializer.save(source, file);
        Contact[] loaded = serializer.load(file);

        TestContacts.assertSameContacts(source, loaded);
    }

    @Test
    void rejectsCorruptedBinaryFile() throws Exception {
        Path file = tempDir.resolve("broken.bin");
        Files.write(file, new byte[]{1, 2, 3, 4});

        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.load(file));

        assertEquals(SerializationErrorCode.BROKEN_FILE, error.getCode());
    }

    @Test
    void rejectsUnsupportedSchemaVersion() throws Exception {
        ContactFile file = new ContactFile();
        file.setSchemaVersion(ContactFile.CURRENT_SCHEMA_VERSION + 1);
        file.setContacts(new ContactDto[0]);
        Path path = tempDir.resolve("future.bin");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
            out.writeObject(file);
        }

        SerializationException error = assertThrows(SerializationException.class,
                () -> serializer.load(path));

        assertEquals(SerializationErrorCode.UNSUPPORTED_VERSION, error.getCode());
    }
}
