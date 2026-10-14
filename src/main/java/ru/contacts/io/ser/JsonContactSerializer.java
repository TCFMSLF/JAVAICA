package ru.contacts.io.ser;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON-сериализатор на Jackson (как JsonBookSerializer из задания).
 * Формат файла: {"schema_version":1,"contacts":[...]}.
 * Отсутствие метки версии — строгая ошибка BAD_VERSION.
 */
public final class JsonContactSerializer implements Serializer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(ContactData data, Path file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), data);
    }

    @Override
    public ContactData load(Path file) throws IOException {
        ContactData data;
        try {
            data = mapper.readValue(file.toFile(), ContactData.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "битый JSON: " + e.getOriginalMessage(), e);
        }
        if (data == null) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "пустой JSON");
        }
        checkVersion(data.getSchemaVersion());
        // Проверяем записи сразу: неизвестный тип и пустые поля —
        // ошибки формата, а не отложенные проблемы GUI.
        data.toModels();
        return data;
    }

    private static void checkVersion(int version) throws SerializationException {
        if (version == ContactData.CURRENT_SCHEMA) {
            return;
        }
        if (version > ContactData.CURRENT_SCHEMA) {
            throw new SerializationException(SerializationException.Code.FUTURE_VERSION,
                    "файл версии " + version + ", программа понимает " + ContactData.CURRENT_SCHEMA);
        }
        throw new SerializationException(SerializationException.Code.BAD_VERSION,
                "нет метки версии или версия " + version
                        + ", ожидается " + ContactData.CURRENT_SCHEMA);
    }
}
