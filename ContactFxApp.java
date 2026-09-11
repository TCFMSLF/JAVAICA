import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Главное окно GUI на JavaFX «Справочник контактов».
 *
 * <p>Покрывает требования:
 * <ul>
 *   <li>отображение элементов — {@code TableView} со всеми контактами;</li>
 *   <li>загрузить из CSV — кнопка + {@link CsvLoader}, ошибки — диалогом;</li>
 *   <li>сохранить в CSV — кнопка + {@link CsvSaver}, ошибки — диалогом;</li>
 *   <li>редактирование сущности (не read-only) — кнопка «Редактировать»;
 *       read-only тип ({@code emergency}, без {@code Editable},
 *       сеттеры заблокированы) блокируется диалогом с ошибкой;</li>
 *   <li>добавление сущности — кнопка «Добавить» через {@link ContactFactory};</li>
 *   <li>диалоги с ошибками — везде через {@code Alert(ERROR)}.</li>
 * </ul>
 * Плюс: поиск по префиксу имени на {@link ContactTrie}
 * (автодополнение + подсчёт в поддереве) с фильтрацией таблицы.
 */
public class ContactFxApp extends Application {

    private final ObservableList<Contact> data = FXCollections.observableArrayList();
    private final TableView<Contact> table = new TableView<>(data);
    private final Label status = new Label("Готово");
    private final TextField searchField = new TextField();
    private final Label searchInfo = new Label("Всего: 0");

    private final CsvLoader loader = new CsvLoader();
    private ContactTrie trie = new ContactTrie();
    private Stage stage;
    private File lastDir = new File(".").getAbsoluteFile();

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Справочник контактов (JavaFX)");

        buildTable();

        Button loadBtn = new Button("Загрузить из CSV");
        Button saveBtn = new Button("Сохранить в CSV");
        Button addBtn = new Button("Добавить");
        Button editBtn = new Button("Редактировать");
        Button delBtn = new Button("Удалить");
        loadBtn.setOnAction(e -> onLoad());
        saveBtn.setOnAction(e -> onSave());
        addBtn.setOnAction(e -> onAdd());
        editBtn.setOnAction(e -> onEdit());
        delBtn.setOnAction(e -> onDelete());

        FlowPane toolbar = new FlowPane(8, 8, loadBtn, saveBtn, addBtn, editBtn, delBtn);
        toolbar.setPadding(new Insets(8));

        searchField.setPromptText("Префикс имени");
        searchField.setPrefColumnCount(15);
        Button searchBtn = new Button("Найти");
        searchBtn.setOnAction(e -> onSearch());
        searchField.setOnAction(e -> onSearch());
        HBox searchBox = new HBox(8, new Label("Префикс:"), searchField, searchBtn, searchInfo);
        searchBox.setPadding(new Insets(0, 8, 8, 8));
        TitledPane searchPane = new TitledPane("Поиск по имени (Trie)", searchBox);
        searchPane.setCollapsible(false);

        VBox top = new VBox(toolbar, searchPane);

        VBox.setVgrow(table, Priority.ALWAYS);
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(status);
        BorderPane.setMargin(status, new Insets(4, 8, 4, 8));

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                onEdit();
            }
        });

        rebuildTrie();
        primaryStage.setScene(new Scene(root, 950, 550));
        primaryStage.show();
    }

    private void buildTable() {
        TableColumn<Contact, String> typeCol = col("Тип", 90, ContactFxApp::typeOf);
        TableColumn<Contact, String> nameCol = col("Имя", 170, Contact::getName);
        TableColumn<Contact, String> phoneCol = col("Телефон", 140, Contact::getPhone);
        TableColumn<Contact, String> emailCol = col("E-mail", 190, Contact::getEmail);
        TableColumn<Contact, String> orgCol = col("Организация", 130, Contact::getOrganization);
        TableColumn<Contact, String> posCol = col("Должность", 110,
                c -> c instanceof CorporateContact cc ? cc.getPosition() : "");
        TableColumn<Contact, String> numCol = col("Внутр. номер", 100,
                c -> c instanceof CorporateContact cc ? cc.getInternalNumber() : "");
        TableColumn<Contact, String> edCol = col("Изм.", 80,
                c -> c instanceof EmergencyContact ? "read-only" : "да");
        table.getColumns().addAll(
                List.of(typeCol, nameCol, phoneCol, emailCol, orgCol, posCol, numCol, edCol));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private static TableColumn<Contact, String> col(String title, double width,
                                                    java.util.function.Function<Contact, String> f) {
        TableColumn<Contact, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(p -> new ReadOnlyStringWrapper(nn(f.apply(p.getValue()))));
        return c;
    }

    // ---------- CSV ----------

    private void onLoad() {
        File file = chooseFile(true);
        if (file == null) {
            return;
        }
        if (!file.isFile()) {
            error("Файл не найден:\n" + file);
            return;
        }
        final CsvLoadResult result;
        try {
            result = loader.load(file.toPath());
        } catch (Exception ex) {
            error("Не удалось прочитать CSV-файл:\n" + ex.getMessage());
            return;
        }
        List<Contact> loaded = result.contacts();
        List<CsvRowException> rowErrors = result.errors();
        if (loaded.isEmpty()) {
            if (!confirm("В файле нет валидных контактов (все строки битые или файл пуст).\n"
                    + "Заменить текущую таблицу пустым списком?")) {
                return;
            }
        } else if (!data.isEmpty()) {
            if (!confirm("Заменить текущие данные (" + data.size()
                    + ") загруженными (" + loaded.size() + ")?")) {
                return;
            }
        }
        data.setAll(loaded);
        rebuildTrie();
        setStatus("Загружено из " + file.getName() + ": " + loaded.size()
                + ", пропущено строк: " + rowErrors.size());
        showLoadReport(file.getName(), loaded.size(), rowErrors);
    }

    private void onSave() {
        if (data.isEmpty()) {
            error("Таблица пуста — нечего сохранять.");
            return;
        }
        File file = chooseFile(false);
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        try {
            Path path = file.toPath();
            if (Files.isDirectory(path)) {
                error("Указан каталог, а не файл:\n" + file);
                return;
            }
            CsvSaver.save(List.copyOf(data), path);
            setStatus("Сохранено в " + file.getName() + ": " + data.size());
            info("Сохранено контактов: " + data.size() + "\nФайл: " + file);
        } catch (Exception ex) {
            error("Не удалось сохранить CSV:\n" + ex.getMessage());
        }
    }

    private File chooseFile(boolean open) {
        FileChooser fc = new FileChooser();
        fc.setTitle(open ? "Загрузить из CSV" : "Сохранить в CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        if (lastDir.isDirectory()) {
            fc.setInitialDirectory(lastDir);
        }
        File chosen = open ? fc.showOpenDialog(stage) : fc.showSaveDialog(stage);
        if (chosen != null && chosen.getParentFile() != null) {
            lastDir = chosen.getParentFile();
        }
        return chosen;
    }

    // ---------- Add / Edit / Delete ----------

    private void onAdd() {
        try {
            Optional<Contact> created = ContactFxDialog.showAdd(stage);
            created.ifPresent(c -> {
                data.add(c);
                rebuildTrie();
                setStatus("Добавлен: " + c.getName());
            });
        } catch (Exception ex) {
            error("Не удалось добавить контакт:\n" + ex.getMessage());
        }
    }

    private void onEdit() {
        Contact original = table.getSelectionModel().getSelectedItem();
        if (original == null) {
            error("Выберите строку в таблице для редактирования.");
            return;
        }
        // Правило варианта: read-only тип (EmergencyContact — без Editable,
        // сеттеры заблокированы исключением) редактировать нельзя.
        // Остальные типы редактируются; для corporate диалог дополнительно
        // проверяет поля через Editable.validate().
        if (original instanceof EmergencyContact) {
            error("Контакт «" + original.getName() + "» — read-only (emergency).\n"
                    + "Редактирование запрещено.");
            return;
        }
        try {
            Optional<Contact> edited = ContactFxDialog.showEdit(stage, original);
            edited.ifPresent(c -> {
                data.set(data.indexOf(original), c);
                rebuildTrie();
                setStatus("Изменён: " + c.getName());
            });
        } catch (UnsupportedOperationException ex) {
            error("Объект read-only, изменение запрещено:\n" + ex.getMessage());
        } catch (Exception ex) {
            error("Не удалось отредактировать контакт:\n" + ex.getMessage());
        }
    }

    private void onDelete() {
        Contact c = table.getSelectionModel().getSelectedItem();
        if (c == null) {
            error("Выберите строку в таблице для удаления.");
            return;
        }
        if (confirm("Удалить контакт «" + c.getName() + "»?")) {
            try {
                data.remove(c);
                rebuildTrie();
                setStatus("Удалён: " + c.getName());
            } catch (Exception ex) {
                error("Не удалось удалить:\n" + ex.getMessage());
            }
        }
    }

    // ---------- Trie-поиск ----------

    private void rebuildTrie() {
        trie = new ContactTrie();
        for (Contact c : data) {
            trie.insert(c);
        }
        onSearch();
    }

    private void onSearch() {
        String prefix = searchField.getText() == null ? "" : searchField.getText().trim();
        if (prefix.isEmpty()) {
            if (table.getItems() != data) {
                table.setItems(data);
            }
            searchInfo.setText("Всего: " + trie.size());
            return;
        }
        try {
            List<Contact> found = trie.autocomplete(prefix);
            int count = trie.countByPrefix(prefix);
            searchInfo.setText("Найдено: " + count);
            table.setItems(FXCollections.observableArrayList(found));
        } catch (Exception ex) {
            error("Ошибка поиска:\n" + ex.getMessage());
        }
    }

    // ---------- helpers ----------

    static String typeOf(Contact c) {
        if (c instanceof EmergencyContact) {
            return "emergency";
        }
        if (c instanceof CorporateContact) {
            return "corporate";
        }
        return "contact";
    }

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    /** Итог загрузки: сколько принято, какие строки пропущены и по каким кодам. */
    private void showLoadReport(String fileName, int loaded, List<CsvRowException> rowErrors) {
        if (rowErrors.isEmpty()) {
            info("Файл: " + fileName + "\nЗагружено контактов: " + loaded);
            return;
        }
        StringBuilder sb = new StringBuilder("Файл: ").append(fileName)
                .append("\nЗагружено контактов: ").append(loaded)
                .append("\nПропущено битых строк: ").append(rowErrors.size());
        int shown = Math.min(rowErrors.size(), 10);
        for (int i = 0; i < shown; i++) {
            CsvRowException e = rowErrors.get(i);
            sb.append("\n  строка ").append(e.getLineNo())
              .append(" [").append(e.getCode()).append("]: ").append(e.getMessage());
        }
        if (rowErrors.size() > shown) {
            sb.append("\n  … и ещё ").append(rowErrors.size() - shown);
        }
        Alert a = new Alert(Alert.AlertType.WARNING, sb.toString(), ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Загрузка завершена с пропуском строк");
        a.showAndWait();
    }

    static void error(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Ошибка");
        a.showAndWait();
    }

    private void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Готово");
        a.showAndWait();
    }

    private boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.setTitle("Подтверждение");
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
