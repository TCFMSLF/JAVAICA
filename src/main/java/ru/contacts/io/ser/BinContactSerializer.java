package ru.contacts.io.ser;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Бинарный сериализатор «из JDK»: класс реализует маркер-интерфейс
 * {@code java.io.Serializable}, поле {@code serialVersionUID} —
 * «паспорт версии» класса (см. задание, деталь 2).
 * Плюс явная метка {@code schemaVersion} внутри {@link ContactData}:
 * файл из будущего отклоняется кодом FUTURE_VERSION.
 */
public final class BinContactSerializer implements Serializer {

    @Override
    public String format() {
        return "bin";
    }

    @Override
    public void save(ContactData data, Path file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(data);
        }
    }

    @Override
    public ContactData load(Path file) throws IOException {
        Object obj;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            obj = in.readObject();
        } catch (InvalidClassException e) {
            throw new SerializationException(SerializationException.Code.BAD_VERSION,
                    "версия класса не совпала: " + e.getMessage(), e);
        } catch (StreamCorruptedException | EOFException e) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "обрезанный бинарный файл: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "неизвестный класс в файле: " + e.getMessage(), e);
        }
        if (!(obj instanceof ContactData)) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "в файле нет ContactData");
        }
        ContactData data = (ContactData) obj;
        if (data.getSchemaVersion() > ContactData.CURRENT_SCHEMA) {
            throw new SerializationException(SerializationException.Code.FUTURE_VERSION,
                    "файл версии " + data.getSchemaVersion()
                            + ", программа понимает " + ContactData.CURRENT_SCHEMA);
        }
        if (data.getSchemaVersion() != ContactData.CURRENT_SCHEMA) {
            throw new SerializationException(SerializationException.Code.BAD_VERSION,
                    "версия схемы " + data.getSchemaVersion()
                            + ", ожидается " + ContactData.CURRENT_SCHEMA);
        }
        data.toModels();
        return data;
    }
}
