package ru.contacts.store;

import ru.contacts.model.Contact;

/**
 * Фабрика хранилищ контактов: ключ — {@link Contact#getName()} как есть
 * (case-sensitive, без нормализации), дубликат перезаписывает значение.
 */
public final class ContactStores {

    private ContactStores() {
    }

    public static TrieStore<Contact> newStore() {
        return new TrieStore<>(Contact::getName, Contact.class);
    }
}
