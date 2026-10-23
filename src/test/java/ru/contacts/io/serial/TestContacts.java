package ru.contacts.io.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/** Общие тестовые данные и проверка равенства контактов. */
final class TestContacts {

    private TestContacts() {
    }

    static Contact[] sample() {
        return new Contact[]{
                new Contact("Иван Петров", "+7-900-111-22-33", "ivan@example.com", "ООО Ромашка"),
                new EmergencyContact("Мама", "+7-900-000-00-01", "mama@example.com", ""),
                new CorporateContact("Анна Сидорова", "+7-900-222-33-44", "anna@corp.ru",
                        "ООО Ромашка", "Менеджер", "1234"),
                new Contact("Пётр Смирнов", "+7-900-333-44-55", "petr@example.com", null)
        };
    }

    static void assertSameContacts(Contact[] expected, Contact[] actual) {
        assertNotNull(actual);
        assertEquals(expected.length, actual.length, "размер массива контактов");
        for (int i = 0; i < expected.length; i++) {
            assertSameContact(expected[i], actual[i], i);
        }
    }

    private static void assertSameContact(Contact expected, Contact actual, int index) {
        assertNotNull(actual, "контакт #" + index);
        assertEquals(expected.getClass(), actual.getClass(), "тип контакта #" + index);
        assertEquals(expected.getName(), actual.getName(), "имя контакта #" + index);
        assertEquals(expected.getPhone(), actual.getPhone(), "телефон контакта #" + index);
        assertEquals(expected.getEmail(), actual.getEmail(), "e-mail контакта #" + index);
        assertEquals(expected.getOrganization(), actual.getOrganization(), "организация контакта #" + index);
        if (expected instanceof CorporateContact expectedCorporate) {
            CorporateContact actualCorporate = (CorporateContact) actual;
            assertEquals(expectedCorporate.getPosition(), actualCorporate.getPosition(),
                    "должность контакта #" + index);
            assertEquals(expectedCorporate.getInternalNumber(), actualCorporate.getInternalNumber(),
                    "внутренний номер контакта #" + index);
        }
    }
}
