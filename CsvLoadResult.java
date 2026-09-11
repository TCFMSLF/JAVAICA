import java.util.List;

/**
 * Результат загрузки CSV: успешно разобранные контакты
 * плюс причины пропуска битых строк.
 *
 * <p>Битые строки больше не теряются в консоли — GUI показывает
 * {@link #errors()} в диалоге «Загружено N, пропущено M».
 */
public record CsvLoadResult(List<Contact> contacts, List<CsvRowException> errors) {

    public CsvLoadResult {
        contacts = List.copyOf(contacts);
        errors = List.copyOf(errors);
    }
}
