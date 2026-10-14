package ru.contacts.io.ser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ru.contacts.model.Contact;

/**
 * Единица обмена сериализаторов (аналог BookData из задания):
 * метка версии схемы + записи. Один и тот же класс едет
 * и в JSON (Jackson), и в BIN ({@code Serializable}).
 */
public final class ContactData implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** Текущая версия схемы. */
    public static final int CURRENT_SCHEMA = 1;

    @JsonProperty("schema_version")
    private int schemaVersion;

    private List<ContactDto> contacts;

    /** No-arg для Jackson и Serializable. */
    public ContactData() {
        this.schemaVersion = CURRENT_SCHEMA;
        this.contacts = new ArrayList<>();
    }

    public ContactData(int schemaVersion, List<ContactDto> contacts) {
        this.schemaVersion = schemaVersion;
        this.contacts = contacts;
    }

    /** Снимок моделей → данные (для save). */
    public static ContactData fromModels(List<Contact> models) {
        return new ContactData(CURRENT_SCHEMA, ContactDto.fromModels(models));
    }

    /** Данные → модели (для load в хранилище). */
    public List<Contact> toModels() throws SerializationException {
        if (contacts == null) {
            throw new SerializationException(SerializationException.Code.TRUNCATED,
                    "нет списка контактов");
        }
        return ContactDto.toModels(contacts);
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<ContactDto> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactDto> contacts) {
        this.contacts = contacts;
    }
}
