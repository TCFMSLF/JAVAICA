/**
 * Аварийный контакт (read-only) — наследник {@link Contact} без {@link Editable}.
 *
 * <p>Неизменяемость: сеттеры перекрыты и заблокированы исключением,
 * новых изменяемых полей нет. Кнопка «Изменить»
 * ({@code entity instanceof Editable}) для него блокируется.
 *
 * <p>Создание: только загрузкой из файла ({@link CsvLoader});
 * диалог «Добавить» этот тип не предлагает ({@link ContactFactory#ADDABLE_TYPES}),
 * прямой вызов фабрики с {@code emergency} отклоняется исключением.
 */
public final class EmergencyContact extends Contact {
    public EmergencyContact(String name, String phone, String email, String organization) {
        super(name, phone, email, organization);
    }

    @Override
    public final void setName(String name) {
        throw new UnsupportedOperationException("EmergencyContact is read-only");
    }

    @Override
    public final void setPhone(String phone) {
        throw new UnsupportedOperationException("EmergencyContact is read-only");
    }

    @Override
    public final void setEmail(String email) {
        throw new UnsupportedOperationException("EmergencyContact is read-only");
    }

    @Override
    public final void setOrganization(String organization) {
        throw new UnsupportedOperationException("EmergencyContact is read-only");
    }

    @Override
    public String toString() {
        return "EmergencyContact{" +
                "name='" + getName() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", organization='" + getOrganization() + '\'' +
                '}';
    }
}
