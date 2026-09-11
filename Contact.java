/**
 * Основная сущность варианта «Справочник контактов» — базовая сущность иерархии.
 *
 * <p>Обычный класс данных: поля, конструктор, геттеры/сеттеры, toString.
 * Наследование ({@code extends}) ниже переиспользует код и поля этого класса;
 * контракт редактирования ({@code Editable}) — только у производного
 * редактируемого типа, сам базовый тип его не реализует (как Book в примере
 * из методички). Базовый контакт при этом изменяем через сеттеры —
 * read-only у нас только {@link EmergencyContact}.
 *
 * <p>Так сделано намеренно: если бы {@code Editable} реализовал базовый класс,
 * подтип {@code EmergencyContact} наследовал бы его автоматически
 * (в Java интерфейс у подтипа не «отобрать»), и проверка кнопки «Изменить»
 * ({@code entity instanceof Editable}) перестала бы отличать read-only.
 */
public class Contact {
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
        return "Contact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
