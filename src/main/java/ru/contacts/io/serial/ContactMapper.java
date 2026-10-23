package ru.contacts.io.serial;

import java.util.List;
import java.util.Locale;

import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.EmergencyContact;

/**
 * Преобразование доменных контактов в DTO и обратно.
 * В файл попадает только снимок контактов; Trie при импорте пересобирается.
 */
final class ContactMapper {

    private ContactMapper() {
    }

    static ContactFile toFile(Contact[] contacts) throws SerializationException {
        if (contacts == null) {
            throw new SerializationException(SerializationErrorCode.INVALID_CONTACT_DATA,
                    "Список контактов не задан");
        }
        ContactDto[] dtos = new ContactDto[contacts.length];
        for (int i = 0; i < contacts.length; i++) {
            if (contacts[i] == null) {
                throw new SerializationException(SerializationErrorCode.INVALID_CONTACT_DATA,
                        "Контакт #" + i + " равен null");
            }
            List<String> errors = contacts[i].validate();
            if (!errors.isEmpty()) {
                throw new SerializationException(SerializationErrorCode.INVALID_CONTACT_DATA,
                        "Контакт #" + i + " (" + contacts[i].getName() + "): "
                                + String.join("; ", errors));
            }
            dtos[i] = toDto(contacts[i]);
        }
        return new ContactFile(ContactFile.CURRENT_SCHEMA_VERSION, dtos);
    }

    static Contact[] toContacts(ContactFile file) throws SerializationException {
        if (file == null) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "Файл не содержит корневого объекта");
        }
        if (file.getSchemaVersion() != ContactFile.CURRENT_SCHEMA_VERSION) {
            throw new SerializationException(SerializationErrorCode.UNSUPPORTED_VERSION,
                    "Версия схемы " + file.getSchemaVersion() + " не поддерживается (ожидается "
                            + ContactFile.CURRENT_SCHEMA_VERSION + ")");
        }
        if (file.getContacts() == null) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "В файле отсутствует массив контактов");
        }
        ContactDto[] dtos = file.getContacts();
        Contact[] contacts = new Contact[dtos.length];
        for (int i = 0; i < dtos.length; i++) {
            contacts[i] = fromDto(dtos[i], i);
        }
        return contacts;
    }

    private static ContactDto toDto(Contact contact) {
        String type;
        if (contact instanceof CorporateContact) {
            type = "CORPORATE";
        } else if (contact instanceof EmergencyContact) {
            type = "EMERGENCY";
        } else {
            type = "CONTACT";
        }
        String position = contact instanceof CorporateContact corporate
                ? corporate.getPosition() : null;
        String internalNumber = contact instanceof CorporateContact corporate
                ? corporate.getInternalNumber() : null;
        return new ContactDto(type, contact.getName(), contact.getPhone(), contact.getEmail(),
                contact.getOrganization(), position, internalNumber);
    }

    private static Contact fromDto(ContactDto dto, int index) throws SerializationException {
        if (dto == null) {
            throw new SerializationException(SerializationErrorCode.BROKEN_FILE,
                    "Контакт #" + index + " отсутствует");
        }
        String type = dto.getType() == null
                ? "" : dto.getType().trim().toUpperCase(Locale.ROOT);
        String name = dto.getName();
        String phone = dto.getPhone();
        String email = dto.getEmail();
        String organization = dto.getOrganization();

        Contact contact;
        switch (type) {
            case "CONTACT" -> contact = new Contact(name, phone, email, organization);
            case "EMERGENCY" -> contact = new EmergencyContact(name, phone, email, organization);
            case "CORPORATE" -> contact = new CorporateContact(name, phone, email, organization,
                    dto.getPosition(), dto.getInternalNumber());
            default -> throw new SerializationException(SerializationErrorCode.INVALID_CONTACT_DATA,
                    "Контакт #" + index + ": неизвестный тип «" + dto.getType() + "»");
        }

        List<String> errors = contact.validate();
        if (!errors.isEmpty()) {
            throw new SerializationException(SerializationErrorCode.INVALID_CONTACT_DATA,
                    "Контакт #" + index + " (" + name + "): " + String.join("; ", errors));
        }
        return contact;
    }
}
