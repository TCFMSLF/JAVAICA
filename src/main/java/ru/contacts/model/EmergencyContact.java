package ru.contacts.model;

/**
 * Аварийный контакт — read-only тип варианта 4.
 *
 * <p>НЕ реализует {@link Editable}, сеттеров нет: редактирование запрещено.
 * Объекты этого типа появляются только при загрузке из файла, в GUI их создать нельзя.
 *
 * <p>Класс намеренно не имеет собственных полей: он помечает объект как read-only,
 * и отдельные сеттеры с {@code UnsupportedOperationException} не нужны — базовый
 * {@link Contact} неизменяемый.
 */
public class EmergencyContact extends Contact {

    public EmergencyContact(String name, String phone, String email, String organization) {
        super(name, phone, email, organization);
    }

    @Override
    public ContactType type() {
        return ContactType.EMERGENCY;
    }
}
