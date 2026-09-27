package ru.contacts.model;

// Аварийный контакт — read-only тип варианта 4: не реализует Editable, поэтому кнопка
// «Изменить» на такой строке заблокирована. Через GUI не создаётся — только из файла.
//
// Собственных полей класс намеренно не имеет: его роль — запретить редактирование.
// Раньше здесь были сеттеры с UnsupportedOperationException, но они ничего не
// защищали: поля базового Contact и так private, а объект неизменяем.
public class EmergencyContact extends Contact {

    public EmergencyContact(String name, String phone, String email, String organization) {
        super(name, phone, email, organization);
    }

    @Override
    public ContactType type() {
        return ContactType.EMERGENCY;
    }
}
