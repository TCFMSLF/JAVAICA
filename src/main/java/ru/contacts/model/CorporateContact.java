package ru.contacts.model;

import java.util.List;

// Корпоративный контакт — редактируемый тип с дополнительными полями.
//
// Единственная сущность варианта 4, реализующая Editable: по условию задачи
// только такие контакты можно создать и изменить через GUI.
public class CorporateContact extends Contact implements Editable {

    private final String position;
    private final String internalNumber;

    public CorporateContact(String name, String phone, String email, String organization,
                            String position, String internalNumber) {
        super(name, phone, email, organization);
        this.position = position;
        this.internalNumber = internalNumber;
    }

    public String getPosition() {
        return position;
    }

    public String getInternalNumber() {
        return internalNumber;
    }

    @Override
    public ContactType type() {
        return ContactType.CORPORATE;
    }

    @Override
    public String toString() {
        return describe("position='" + position + '\'',
                "internalNumber='" + internalNumber + '\'');
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();
        if (position == null || position.isBlank()) {
            errors.add("Должность не должна быть пустой");
        }
        return errors;
    }
}
