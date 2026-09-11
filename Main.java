import javafx.application.Application;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Класс данных: пара объектов основной сущности через «Добавить» (только редактируемые типы)
        Contact c1 = ContactFactory.create("contact", "Анна Смирнова", "+7-900-111-22-33",
                "anna.smirnova@example.com", "ООО Ромашка", "", "");
        Contact c2 = ContactFactory.create("contact", "Андрей Соколов", "+7-900-444-55-66",
                "andrey.sokolov@example.com", "АО Вектор", "", "");
        System.out.println(c1);
        System.out.println(c2);

        // Редактируемый наследник с дополнительными полями — тоже через «Добавить»
        Contact cc = ContactFactory.create("corporate", "Антон Смирнов", "+7-495-123-45-67",
                "a.smirnov@vector.ru", "АО Вектор", "Менеджер", "101");
        System.out.println(cc);

        // Read-only через «Добавить» запрещён: появляется только из файла
        try {
            ContactFactory.create("emergency", "Иван Смирнов (SOS)", "+7-900-000-00-01",
                    "sos.smirnov@example.com", "—", "", "");
        } catch (IllegalArgumentException e) {
            System.out.println("Добавить/emergency запрещено: " + e.getMessage());
        }

        // 2. Загрузка CSV (единственный источник read-only); битые строки пропускаются с кодами
        CsvLoader loader = new CsvLoader();
        CsvLoadResult loadResult;
        try {
            loadResult = loader.load(Path.of("contacts.csv"));
        } catch (java.io.IOException e) {
            System.err.println("Ошибка чтения CSV-файла: " + e.getMessage());
            loadResult = new CsvLoadResult(List.of(), List.of());
        }
        List<Contact> fromCsv = loadResult.contacts();
        System.out.println("Загружено из CSV: " + fromCsv.size()
                + ", пропущено битых строк: " + loadResult.errors().size());
        for (CsvRowException e : loadResult.errors()) {
            System.out.println("  Пропущена: " + e.getMessage());
        }
        for (Contact c : fromCsv) {
            System.out.println(c);
        }

        // Read-only берём из файла, а не создаём напрямую
        Contact ec = null;
        for (Contact c : fromCsv) {
            if (c instanceof EmergencyContact) {
                ec = c;
                break;
            }
        }
        System.out.println("Аварийный контакт (из файла): " + ec);

        // 3. Хранилище: список созданных объектов + проход циклом
        List<Contact> contacts = new ArrayList<>();
        contacts.add(c1);
        contacts.add(c2);
        contacts.add(cc);
        contacts.addAll(fromCsv);
        System.out.println("Хранилище контактов:");
        for (Contact c : contacts) {
            System.out.println(c);
        }

        // Read-only: попытка изменения аварийного контакта запрещена
        if (ec == null) {
            System.out.println("В файле нет аварийного контакта — демо read-only пропущено");
        } else {
            try {
                ec.setPhone("+7-000-000-00-00");
            } catch (UnsupportedOperationException e) {
                System.out.println("EmergencyContact read-only: " + e.getMessage());
            }
        }

        // Editable: контракт с валидацией — только у corporate;
        // кнопка «Изменить» блокируется для read-only (emergency без Editable),
        // базовый contact меняется через сеттеры (валидация — в ContactFactory)
        System.out.println("Проверка Editable/validate:");
        for (Contact c : contacts) {
            if (c instanceof EmergencyContact) {
                System.out.println("  Read-only, кнопка «Изменить» заблокирована: " + c.getName());
            } else if (c instanceof Editable editable) {
                System.out.println("  Editable: " + c.getName() + " -> ошибки: " + editable.validate());
            } else {
                System.out.println("  Изменяемый (без Editable): " + c.getName());
            }
        }
        System.out.println("  Типы в «Добавить»: " + ContactFactory.ADDABLE_TYPES);

        // 4. Структура данных: Trie по имени
        ContactTrie trie = new ContactTrie();
        for (Contact c : contacts) {
            trie.insert(c);
        }

        // 5. Дополнительные операции: автодополнение и подсчёт в поддереве
        String prefix = "Ан";
        List<Contact> found = trie.autocomplete(prefix);
        System.out.println("Автодополнение по префиксу \"" + prefix + "\":");
        for (Contact c : found) {
            System.out.println("  " + c);
        }
        System.out.println("Контактов в поддереве \"" + prefix + "\": " + trie.countByPrefix(prefix));
        System.out.println("Всего контактов: " + trie.size());

        // 6. GUI на JavaFX: запуск графического интерфейса
        Application.launch(ContactFxApp.class, args);
    }
}
