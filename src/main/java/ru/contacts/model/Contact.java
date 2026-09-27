package ru.contacts.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Основная сущность варианта 4: контакт справочника.
 *
 * <p>Класс неизменяемый: поля {@code private final}, сеттеров нет. Правка выполняется
 * созданием нового объекта, поэтому read-only подтип {@link EmergencyContact} не может
 * быть изменён в обход контракта.
 *
 * <p>Базовая сущность намеренно НЕ реализует {@link Editable}: по условию задачи редактируемым
 * является только корпоративный контакт.
 */
public class Contact {

    private final String name;
    private final String phone;
    private final String email;
    private final String organization;

    public Contact(String name, String phone, String email, String organization) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganization() {
        return organization;
    }

    /**
     * @return тип контакта для отображения и записи в файл
     */
    public ContactType type() {
        return ContactType.CONTACT;
    }

    /**
     * Собирает строковое представление: имя класса, поля базового типа и дополнительные поля.
     * Реализовано один раз здесь, чтобы наследники не дублировали форматирование.
     *
     * @param extraFields дополнительные поля в виде {@code "поле='значение'"}
     */
    protected String describe(String... extraFields) {
        StringJoiner joiner = new StringJoiner(", ", getClass().getSimpleName() + "{", "}");
        joiner.add("name='" + name + '\'');
        joiner.add("phone='" + phone + '\'');
        joiner.add("email='" + email + '\'');
        joiner.add("organization='" + organization + '\'');
        for (String field : extraFields) {
            joiner.add(field);
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return describe();
    }

    /**
     * Проверка полей контакта. Есть у всех типов, чтобы правила не зависели от JavaFX.
     * Для {@link CorporateContact} переопределяется и дополняется проверкой должности.
     *
     * @return список ошибок; пустой список = данные корректны
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (name == null || name.isBlank()) {
            errors.add("Имя не должно быть пустым");
        }
        if (phone == null || phone.isBlank()) {
            errors.add("Телефон не должен быть пустым");
        }
        if (email == null || !email.contains("@")) {
            errors.add("E-mail должен содержать '@'");
        }
        return errors;
    }
}
