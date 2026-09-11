import java.util.ArrayList;
import java.util.List;

/**
 * Корпоративный контакт — редактируемый наследник {@link Contact}
 * с дополнительными полями «должность» и «внутренний номер».
 *
 * <p>Наследование ({@code extends}) переиспользует код и поля базового класса;
 * интерфейс ({@code implements Editable}) — только контракт без состояния,
 * его можно комбинировать с другими интерфейсами.
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

    /** Проверка всех полей: базовых (имя/телефон/e-mail) и новых (должность/внутренний номер). */
    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (getName() == null || getName().isBlank()) {
            errors.add("Имя не должно быть пустым");
        }
        if (getPhone() == null || getPhone().isBlank()) {
            errors.add("Телефон не должен быть пустым");
        }
        if (getEmail() != null && !getEmail().isBlank() && !getEmail().contains("@")) {
            errors.add("E-mail должен содержать '@'");
        }
        if (position == null || position.isBlank()) {
            errors.add("Должность не должна быть пустой");
        }
        if (internalNumber == null || internalNumber.isBlank()) {
            errors.add("Внутренний номер не должен быть пустым");
        }
        return errors;
    }

    @Override
    public String toString() {
        return "CorporateContact{" +
                "name='" + getName() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", organization='" + getOrganization() + '\'' +
                ", position='" + position + '\'' +
                ", internalNumber='" + internalNumber + '\'' +
                '}';
    }
}
