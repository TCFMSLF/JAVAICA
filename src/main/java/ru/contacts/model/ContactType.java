package ru.contacts.model;

import java.util.Optional;

// Тип контакта варианта 4.
//
// Единственное место в проекте, где хранится соответствие «класс модели <-> тег в файле».
// Раньше эти строки повторялись в CsvLoader, ContactDialog и ContactApp.
public enum ContactType {
    CONTACT,
    EMERGENCY,
    CORPORATE;

    public static Optional<ContactType> fromTag(String tag) {
        for (ContactType type : values()) {
            if (type.tag().equals(tag)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static ContactType of(Contact contact) {
        if (contact instanceof CorporateContact) {
            return CORPORATE;
        }
        if (contact instanceof EmergencyContact) {
            return EMERGENCY;
        }
        return CONTACT;
    }

    // Отдельного поля tag нет: тег в CSV совпадает с именем константы.
    // Хранить одно и то же дважды — путь к рассинхрону.
    public String tag() {
        return name();
    }
}
