package ru.contacts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Основная сущность варианта 4: контакт справочника.
 */

public class Contact implements Editable {
    protected String name;
    protected String phone;
    protected String email;
    protected String organization;

    public Contact(String name, String phone, String email, String organization) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{name='" + name + '\''
                + ", phone='" + phone + '\''
                + ", email='" + email + '\''
                + ", organization='" + organization + '\''
                + '}';
    }

    @Override
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
