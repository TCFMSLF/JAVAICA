package ru.contacts.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

// Основная сущность варианта 4: контакт справочника.
//
// НЕ реализует Editable: по условию задания редактируемым является только корпоративный
// контакт. Если реализовать интерфейс здесь, EmergencyContact унаследует его
// и станет редактируемым — а он должен быть read-only.
public class Contact {

    // final, а не просто private: неизменяемость проверяет компилятор, а не код-ревью.
    // Сеттеров нет намеренно — правка выполняется созданием нового объекта.
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

    // Не abstract, хотя базовый тип: Contact создаётся напрямую, в том числе из CSV.
    // Это полноценная сущность, а не абстракция для наследования.
    public ContactType type() {
        return ContactType.CONTACT;
    }

    // Собирает строковое представление. extraFields приходят в виде "поле='значение'",
    // поэтому форматирование собрано один раз здесь, а не дублируется в наследниках.
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

    // Правила проверки — в модели, а не в GUI, чтобы не тянуть JavaFX в консольные лабы.
    // Пустой список = данные корректны.
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
