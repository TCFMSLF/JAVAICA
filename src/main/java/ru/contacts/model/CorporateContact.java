package ru.contacts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Корпоративный контакт — редактируемый тип с новыми полями.
 */
public class CorporateContact extends Contact implements Editable {
    private String position;
    private String internalNumber;

    public CorporateContact(String name, String phone, String email, String organization,
                            String position, String internalNumber) {
        super(name, phone, email, organization);
        this.position = position;
        this.internalNumber = internalNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getInternalNumber() {
        return internalNumber;
    }

    public void setInternalNumber(String internalNumber) {
        this.internalNumber = internalNumber;
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
        if (position == null || position.isBlank()) {
            errors.add("Должность не должна быть пустой");
        }
        return errors;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{name='" + name + '\''
                + ", phone='" + phone + '\''
                + ", email='" + email + '\''
                + ", organization='" + organization + '\''
                + ", position='" + position + '\''
                + ", internalNumber='" + internalNumber + '\''
                + '}';
    }
}
