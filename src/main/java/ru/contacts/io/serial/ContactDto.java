package ru.contacts.io.serial;

import java.io.Serializable;

/**
 * Транспортное представление контакта для JSON и бинарного формата.
 * Одинаковая DTO-модель для обоих сериализаторов сохраняет
 * полиморфизм базового, read-only и корпоративного контактов.
 */
public class ContactDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String name;
    private String phone;
    private String email;
    private String organization;
    private String position;
    private String internalNumber;

    public ContactDto() {
    }

    public ContactDto(String type, String name, String phone, String email,
                      String organization, String position, String internalNumber) {
        this.type = type;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organization = organization;
        this.position = position;
        this.internalNumber = internalNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
