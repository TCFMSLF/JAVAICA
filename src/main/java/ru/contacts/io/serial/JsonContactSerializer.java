package ru.contacts.io.serial;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.contacts.model.Contact;

/**
 * JSON-формат: человекочитаемый снимок контактов через Jackson.
 * Версия схемы хранится в корневом объекте {@link ContactFile}.
 */
public class JsonContactSerializer implements Serializer<Contact[]> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(Contact[] contacts, Path file) throws IOException {
        ContactFile root = ContactMapper.toFile(contacts);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);
    }

    @Override
    public Contact[] load(Path file) throws IOException {
        ContactFile root;
        try {
            root = MAPPER.readValue(file.toFile(), ContactFile.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "Некорректный JSON: " + e.getOriginalMessage(), e);
        }
        return ContactMapper.toContacts(root);
    }
}
