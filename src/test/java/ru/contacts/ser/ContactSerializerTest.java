package ru.contacts.ser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.contacts.io.ser.BinContactSerializer;
import ru.contacts.io.ser.ContactData;
import ru.contacts.io.ser.ContactDto;
import ru.contacts.io.ser.JsonContactSerializer;
import ru.contacts.io.ser.SerializationException;
import ru.contacts.io.ser.Serializer;
import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Лаб. 3: сериализация bin + JSON через общий интерфейс {@link Serializer}.
 * Roundtrip-контракт: load(save(x)) возвращает данные, равные x.
 */
class ContactSerializerTest {

    @TempDir
    Path tmp;

    // ---- позитивные ----

    @Test
    void json_loads_fixture_with_three_types() throws Exception {
        ContactData data = new JsonContactSerializer().load(fixture("contacts.json"));

        assertEquals(3, data.getContacts().size());
    }

    @Test
    void json_roundtrip_preserves_all_fields() throws Exception {
        Serializer json = new JsonContactSerializer();
        ContactData data = json.load(fixture("contacts.json"));

        Path file = tmp.resolve("roundtrip.json");
        json.save(data, file);
        ContactData reread = json.load(file);

        assertContactListsEqual(data.toModels(), reread.toModels());
    }

    @Test
    void bin_roundtrip_preserves_all_fields() throws Exception {
        Serializer bin = new BinContactSerializer();
        ContactData data = sampleData();

        Path file = tmp.resolve("roundtrip.bin");
        bin.save(data, file);
        ContactData reread = bin.load(file);

        assertContactListsEqual(data.toModels(), reread.toModels());
    }

    @Test
    void emergency_stays_readonly_after_json_load() throws Exception {
        ContactData data = new JsonContactSerializer().load(fixture("contacts.json"));

        Contact found = null;
        for (Contact c : data.toModels()) {
            if (c instanceof EmergencyContact) {
                found = c;
            }
        }

        assertTrue(found != null);
        Contact readOnly = found;
        assertThrows(UnsupportedOperationException.class, () -> readOnly.setName("x"));
    }

    // ---- негативные ----

    @Test
    void json_rejects_missing_schema_version() {
        SerializationException e = assertThrows(SerializationException.class,
                () -> new JsonContactSerializer().load(fixture("noversion.json")));

        assertEquals(SerializationException.Code.BAD_VERSION, e.getCode());
    }

    @Test
    void json_rejects_unknown_type() {
        SerializationException e = assertThrows(SerializationException.class,
                () -> new JsonContactSerializer().load(fixture("unknowntype.json")));

        assertEquals(SerializationException.Code.UNKNOWN_TYPE, e.getCode());
    }

    @Test
    void json_rejects_broken_file() {
        SerializationException e = assertThrows(SerializationException.class,
                () -> new JsonContactSerializer().load(fixture("broken.json")));

        assertEquals(SerializationException.Code.TRUNCATED, e.getCode());
    }

    @Test
    void bin_rejects_truncated_file() throws Exception {
        Path file = tmp.resolve("truncated.bin");
        Files.write(file, new byte[]{0, 1, 2, 3, 4});

        SerializationException e = assertThrows(SerializationException.class,
                () -> new BinContactSerializer().load(file));

        assertEquals(SerializationException.Code.TRUNCATED, e.getCode());
    }

    @Test
    void bin_rejects_future_schema_version() throws Exception {
        ContactData future = sampleData();
        future.setSchemaVersion(ContactData.CURRENT_SCHEMA + 1);
        Path file = tmp.resolve("future.bin");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(future);
        }

        SerializationException e = assertThrows(SerializationException.class,
                () -> new BinContactSerializer().load(file));

        assertEquals(SerializationException.Code.FUTURE_VERSION, e.getCode());
    }

    // ---- helpers ----

    private Path fixture(String name) throws Exception {
        Path file = tmp.resolve(name);
        try (InputStream in = getClass().getResourceAsStream("/" + name)) {
            if (in == null) {
                throw new IllegalStateException("нет фикстуры: " + name);
            }
            Files.write(file, new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }

    private ContactData sampleData() {
        List<Contact> models = new ArrayList<>();
        models.add(new Contact("Иван Петров", "+7-900-111-22-33", "ivan@example.com", "Орг1"));
        models.add(new EmergencyContact("Аварийный Контакт", "+7-900-000-00-01", "sos@example.com", "МЧС"));
        models.add(new CorporateContact("Мария Сидорова", "+7-900-222-33-44",
                "maria@example.com", "Орг2", "Менеджер", "1234"));
        List<ContactDto> dtos = new ArrayList<>();
        for (Contact c : models) {
            dtos.add(ContactDto.fromModel(c));
        }
        return new ContactData(ContactData.CURRENT_SCHEMA, dtos);
    }

    private void assertContactListsEqual(List<Contact> expected, List<Contact> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertContactEquals(expected.get(i), actual.get(i));
        }
    }

    private void assertContactEquals(Contact expected, Contact actual) {
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getOrganization(), actual.getOrganization());
        if (expected instanceof CorporateContact && actual instanceof CorporateContact) {
            assertEquals(((CorporateContact) expected).getPosition(),
                    ((CorporateContact) actual).getPosition());
            assertEquals(((CorporateContact) expected).getInternalNumber(),
                    ((CorporateContact) actual).getInternalNumber());
        }
    }
}
