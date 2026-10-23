package ru.contacts.io.serial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ru.contacts.model.Contact;

/**
 * Бинарный формат: Java-сериализация {@link ContactFile}.
 * Компактнее JSON и быстрее на больших объёмах, но нечитаем человеком.
 */
public class BinaryContactSerializer implements Serializer<Contact[]> {

    @Override
    public String format() {
        return "bin";
    }

    @Override
    public void save(Contact[] contacts, Path file) throws IOException {
        ContactFile root = ContactMapper.toFile(contacts);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(root);
        }
    }

    @Override
    public Contact[] load(Path file) throws IOException {
        Object object;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            object = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "Некорректный бинарный файл", e);
        }
        if (!(object instanceof ContactFile root)) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "Файл не содержит ContactFile");
        }
        return ContactMapper.toContacts(root);
    }
}
