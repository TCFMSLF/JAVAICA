package ru.contacts.model;

import java.util.Optional;

/**
 * Тип контакта варианта 4.
 *
 * <p>Единственное место в проекте, где хранится соответствие «класс модели ↔ тег в файле».
 * Раньше эти строки повторялись в {@code CsvLoader}, {@code ContactDialog} и {@code ContactApp}.
 */
public enum ContactType {
    /** Базовый контакт. */
    CONTACT,
    /** Аварийный контакт, read-only: создаётся и меняется только через GUI-диалог. */
    EMERGENCY,
    /** Корпоративный контакт — единственный редактируемый тип. */
    CORPORATE;

    /**
     * Разбирает тег из первой колонки CSV.
     *
     * @return тип или {@link Optional#empty()}, если тег неизвестен
     */
    public static Optional<ContactType> fromTag(String tag) {
        for (ContactType type : values()) {
            if (type.tag().equals(tag)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Определяет тип конкретного объекта модели.
     */
    public static ContactType of(Contact contact) {
        if (contact instanceof CorporateContact) {
            return CORPORATE;
        }
        if (contact instanceof EmergencyContact) {
            return EMERGENCY;
        }
        return CONTACT;
    }

    /**
     * @return тег для записи в первую колонку CSV
     */
    public String tag() {
        return name();
    }
}
