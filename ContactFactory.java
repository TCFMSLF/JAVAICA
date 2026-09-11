import java.util.List;

/**
 * Единая точка сценария «Добавить» в GUI.
 *
 * <p>Правило варианта: через «Добавить» создаются <b>только редактируемые типы</b>
 * ({@code contact}, {@code corporate}); read-only тип {@code emergency}
 * появляется <b>только из файла</b> (см. {@link CsvLoader}) и здесь
 * отклоняется исключением. Комбобокс диалога «Добавить» заполняется
 * из {@link #ADDABLE_TYPES}, поэтому GUI физически не предлагает
 * read-only тип.
 */
public final class ContactFactory {

    /** Типы, доступные в диалоге «Добавить». Read-only {@code emergency} отсутствует намеренно. */
    public static final List<String> ADDABLE_TYPES = List.of("contact", "corporate");

    private ContactFactory() {
    }

    /**
     * Создаёт редактируемый контакт с проверкой полей.
     *
     * @param type            {@code contact} или {@code corporate}
     * @param position        должность (только corporate, иначе игнорируется)
     * @param internalNumber  внутренний номер (только corporate, иначе игнорируется)
     * @throws IllegalArgumentException при неизвестном/read-only типе или невалидных полях
     */
    public static Contact create(String type, String name, String phone, String email,
                                 String organization, String position, String internalNumber) {
        String t = type == null ? "" : type.trim().toLowerCase();
        switch (t) {
            case "contact": {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("Имя не должно быть пустым");
                }
                if (phone == null || phone.isBlank()) {
                    throw new IllegalArgumentException("Телефон не должен быть пустым");
                }
                if (email != null && !email.isBlank() && !email.contains("@")) {
                    throw new IllegalArgumentException("E-mail должен содержать '@'");
                }
                return new Contact(name, phone, email, organization);
            }
            case "corporate": {
                CorporateContact cc = new CorporateContact(name, phone, email, organization,
                        position, internalNumber);
                List<String> errors = cc.validate();
                if (!errors.isEmpty()) {
                    throw new IllegalArgumentException(String.join("; ", errors));
                }
                return cc;
            }
            case "emergency":
                throw new IllegalArgumentException(
                        "Тип emergency — read-only, создаётся только загрузкой из файла (CsvLoader)");
            default:
                throw new IllegalArgumentException("Неизвестный тип: " + type);
        }
    }
}
