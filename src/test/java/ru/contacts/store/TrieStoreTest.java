package ru.contacts.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;

class TrieStoreTest {

    private TrieStore<Contact> store;

    @BeforeEach
    void setUp() {
        store = ContactStores.newStore();
    }

    @Test
    void addsFindsAndDeletesByKey() {
        store.add(contact("Анна Сидорова"));
        store.add(contact("Борис Петров"));

        assertEquals(2, store.size());
        assertEquals("Анна Сидорова", store.findByKey("Анна Сидорова").getName());
        assertTrue(store.removeByKey("Анна Сидорова"));
        assertEquals(1, store.size());
        assertNull(store.findByKey("Анна Сидорова"));
    }

    @Test
    void autocompleteAndCountInSubtreeReturnMatchingContacts() {
        store.add(contact("Анна Сидорова"));
        store.add(contact("Антон Смирнов"));
        store.add(contact("Борис Петров"));

        Contact[] matches = store.autocomplete("Ан");

        assertEquals(2, matches.length);
        assertEquals(2, store.countInSubtree("Ан"));
        assertEquals(0, store.countInSubtree("О"));
    }

    @Test
    void duplicateKeyOverwritesValueWithoutGrowingSize() {
        store.add(contact("Анна Сидорова"));
        store.add(new Contact("Анна Сидорова", "+7-900-999-99-99", "new@example.com", "Новая"));

        assertEquals(1, store.size());
        assertEquals("+7-900-999-99-99", store.findByKey("Анна Сидорова").getPhone());
    }

    @Test
    void removeKeepsHighlightOnlyOnAttachedAncestors() {
        store.add(contact("abc"));
        store.add(contact("x"));

        store.removeByKey("abc");

        assertEquals(1, store.lastPath().length, "после удаления ветки должен остаться только корень");
        assertSame(store.rootForView(), store.lastPath()[0]);
    }

    private static Contact contact(String name) {
        return new CorporateContact(name, "+7-900-000-00-00", "mail@example.com",
                "Организация", "Должность", "123");
    }
}
