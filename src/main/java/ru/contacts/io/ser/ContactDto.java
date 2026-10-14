package ru.contacts.io.ser;

import java.util.ArrayList;
import java.util.List;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Транспортная запись контакта: плоский DTO с полем {@code type}.
 * Jackson маппит DTO, а не модели напрямую: у моделей нет
 * no-arg конструкторов, а сеттеры {@link EmergencyContact} кидают
 * исключения — bean-маппинг «в лоб» невозможен.
 *
 * type = CONTACT | EMERGENCY | CORPORATE.
 */
public final class ContactDto implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String name;
    private String phone;
    private String email;
    private String organization;
    private String position;
    private String internalNumber;

    /** No-arg для Jackson и Serializable. */
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

    /** Модель → DTO (тип выводится из класса). */
    public static ContactDto fromModel(Contact c) {
        String type = "CONTACT";
        String position = "";
        String internalNumber = "";
        if (c instanceof CorporateContact) {
            type = "CORPORATE";
            position = ((CorporateContact) c).getPosition();
            internalNumber = ((CorporateContact) c).getInternalNumber();
        } else if (c instanceof EmergencyContact) {
            type = "EMERGENCY";
        }
        return new ContactDto(type, c.getName(), c.getPhone(), c.getEmail(),
                c.getOrganization(), position == null ? "" : position,
                internalNumber == null ? "" : internalNumber);
    }

    /** DTO → модель. Объекты создаются конструкторами, сеттеры не вызываются. */
    public Contact toModel() throws SerializationException {
        if (isBlank(name) || isBlank(phone) || isBlank(email)) {
            throw new SerializationException(SerializationException.Code.BAD_FIELD,
                    "имя, телефон и e-mail обязательны: '" + name + "'");
        }
        String org = organization == null ? "" : organization;
        switch (type == null ? "" : type) {
            case "CONTACT":
                return new Contact(name, phone, email, org);
            case "EMERGENCY":
                return new EmergencyContact(name, phone, email, org);
            case "CORPORATE":
                if (isBlank(position)) {
                    throw new SerializationException(SerializationException.Code.BAD_FIELD,
                            "у корпоративного контакта обязательна должность: '" + name + "'");
                }
                return new CorporateContact(name, phone, email, org, position,
                        internalNumber == null ? "" : internalNumber);
            default:
                throw new SerializationException(SerializationException.Code.UNKNOWN_TYPE,
                        "неизвестный тип: '" + type + "'");
        }
    }

    /** Модели → DTO списком. */
    public static List<ContactDto> fromModels(List<Contact> models) {
        List<ContactDto> dtos = new ArrayList<>(models.size());
        for (Contact c : models) {
            dtos.add(fromModel(c));
        }
        return dtos;
    }

    /** DTO → модели списком. */
    public static List<Contact> toModels(List<ContactDto> dtos) throws SerializationException {
        List<Contact> models = new ArrayList<>(dtos.size());
        for (ContactDto d : dtos) {
            models.add(d.toModel());
        }
        return models;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
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
