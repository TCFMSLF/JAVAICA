package ru.contacts.store;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Генератор синтетических контактов для лаб. 2.
 * Имена детерминированы по индексу: "Имя{i} Фамилия{j}",
 * поэтому наборы на 10^4/10^5 не содержат случайных дубликатов.
 */
public final class ContactGenerator {

    private ContactGenerator() {
    }

    public static Contact make(int i) {
        String name = "Имя" + (i / 1000) + "_" + i + " Фамилия" + (i % 1000);
        String phone = "+7-900-" + String.format("%03d", i % 1000) + "-"
                + String.format("%02d", (i / 1000) % 100) + "-"
                + String.format("%02d", i % 100);
        String email = "user" + i + "@example.com";
        String org = "Орг" + (i % 50);
        int kind = i % 10;
        if (kind == 0) {
            return new EmergencyContact(name, phone, email, org);
        }
        if (kind <= 3) {
            return new CorporateContact(name, phone, email, org, "Должность" + (i % 20), String.valueOf(1000 + i % 9000));
        }
        return new Contact(name, phone, email, org);
    }
}
