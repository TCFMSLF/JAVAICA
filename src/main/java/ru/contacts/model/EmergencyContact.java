package ru.contacts.model;

/**
 * Аварийный контакт — read-only тип.
 * НЕ реализует Editable, сеттеров нет: редактирование запрещено.
 * Объекты этого типа появляются только при загрузке из файла.
 */
public class EmergencyContact extends Contact {

    public EmergencyContact(String name, String phone, String email, String organization) {
        super(name, phone, email, organization);
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Аварийный контакт доступен только для чтения");
    }

    @Override
    public void setPhone(String phone) {
        throw new UnsupportedOperationException("Аварийный контакт доступен только для чтения");
    }

    @Override
    public void setEmail(String email) {
        throw new UnsupportedOperationException("Аварийный контакт доступен только для чтения");
    }

    @Override
    public void setOrganization(String organization) {
        throw new UnsupportedOperationException("Аварийный контакт доступен только для чтения");
    }
}
