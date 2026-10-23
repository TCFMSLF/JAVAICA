package ru.contacts.io.serial;

import java.io.Serializable;

/**
 * Корневой объект файла: версия схемы и контакты.
 * Используется и Jackson, и {@link java.io.Serializable}.
 */
public class ContactFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Текущая версия формата. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private int schemaVersion;
    private ContactDto[] contacts;

    public ContactFile() {
    }

    public ContactFile(int schemaVersion, ContactDto[] contacts) {
        this.schemaVersion = schemaVersion;
        this.contacts = contacts;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ContactDto[] getContacts() {
        return contacts;
    }

    public void setContacts(ContactDto[] contacts) {
        this.contacts = contacts;
    }
}
