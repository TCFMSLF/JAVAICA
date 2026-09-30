package ru.contacts.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.contacts.io.CsvLoadResult;
import ru.contacts.io.CsvLoader;
import ru.contacts.io.CsvRowError;
import ru.contacts.model.Contact;
import ru.contacts.model.CorporateContact;
import ru.contacts.model.Editable;
import ru.contacts.store.ContactGenerator;
import ru.contacts.store.ContactStores;
import ru.contacts.store.TrieBenchmark;
import ru.contacts.store.TrieStore;

/**
 * Главное окно (Лаб. 2, вариант 4).
 * Основное хранилище — собственная Trie ({@link TrieStore}, generics),
 * {@link ObservableList} — только витрина для TableView (синхронизируется
 * после каждой операции). Визуализация — {@link TrieCanvas} с полной
 * перерисовкой и подсветкой посещённых узлов.
 */
public class ContactApp extends Application {

    /** Источник правды. Витрина таблицы ниже — лишь его снимок. */
    private final TrieStore<Contact> store = ContactStores.newStore();
    private final ObservableList<Contact> mirror = FXCollections.observableArrayList();

    private TableView<Contact> table;
    private Button editButton;
    private Button deleteButton;
    private TrieCanvas canvas;
    private ProgressBar progress;
    private Label statusLabel;
    private Label searchLabel;
    private Label prefixLabel;
    private TextArea outArea;

    private Task<?> currentTask;

    @Override
    @SuppressWarnings("unchecked")
    public void start(Stage stage) {
        table = new TableView<>(mirror);
        table.setPrefHeight(280);
        table.getColumns().addAll(
                typeColumn(),
                column("Имя", "name", 160),
                column("Телефон", "phone", 130),
                column("E-mail", "email", 160),
                column("Организация", "organization", 130),
                column("Должность", "position", 110),
                column("Внутр. номер", "internalNumber", 90));

        Button loadButton = new Button("Загрузить CSV (фон)");
        Button saveButton = new Button("Сохранить CSV");
        Button addButton = new Button("Добавить");
        editButton = new Button("Изменить");
        deleteButton = new Button("Удалить");
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        loadButton.setOnAction(e -> onLoad(stage));
        saveButton.setOnAction(e -> onSave(stage));
        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        deleteButton.setOnAction(e -> onDelete());
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> {
                    editButton.setDisable(!(newSel instanceof Editable));
                    deleteButton.setDisable(newSel == null);
                });

        HBox crud = new HBox(8, loadButton, saveButton, addButton, editButton, deleteButton);

        // точный поиск
        TextField searchField = new TextField();
        searchField.setPromptText("Точное имя");
        searchField.setPrefWidth(200);
        Button findButton = new Button("Найти");
        searchLabel = new Label("Поиск: —");
        findButton.setOnAction(e -> onFind(searchField.getText()));
        HBox searchBox = new HBox(8, new Label("Поиск:"), searchField, findButton, searchLabel);

        // доп. операции варианта: автодополнение + подсчёт
        TextField prefixField = new TextField();
        prefixField.setPromptText("Префикс");
        prefixField.setPrefWidth(160);
        Button autoButton = new Button("Автодополнение");
        prefixLabel = new Label("Совпадений: —");
        autoButton.setOnAction(e -> onAutocomplete(prefixField.getText()));
        HBox prefixBox = new HBox(8, new Label("Префикс:"), prefixField, autoButton, prefixLabel);

        // генерация
        TextField genField = new TextField("1000");
        genField.setPrefWidth(90);
        Button genButton = new Button("Сгенерировать (фон)");
        Button cancelButton = new Button("Отмена");
        progress = new ProgressBar(0);
        progress.setPrefWidth(220);
        statusLabel = new Label("Trie size: 0");
        genButton.setOnAction(e -> onGenerate(genField.getText()));
        cancelButton.setOnAction(e -> {
            if (currentTask != null) {
                currentTask.cancel();
            }
        });
        HBox genBox = new HBox(8, new Label("Генерация N:"), genField, genButton, cancelButton, progress, statusLabel);

        // визуализация
        canvas = new TrieCanvas(880, 300);
        canvas.widthProperty().addListener((o, a, b) -> refresh());
        HBox canvasBox = new HBox(canvas);

        // демо гонки
        Button raceButton = new Button("Демо гонки");
        raceButton.setOnAction(e -> onRace());
        outArea = new TextArea();
        outArea.setPrefRowCount(4);
        outArea.setEditable(false);
        outArea.setPromptText("Демо гонки...");
        HBox raceBox = new HBox(8, raceButton);

        VBox root = new VBox(8, table, crud, searchBox, prefixBox, genBox, canvasBox, raceBox, outArea);
        root.setPadding(new Insets(12));

        stage.setTitle("Справочник контактов — Trie (Лаб. 2, макс.)");
        stage.setScene(new Scene(root, 920, 830));
        stage.show();
        refresh();
    }

    // ---- CRUD поверх Trie ----

    private void onAdd() {
        Optional<Contact> created = new ContactDialog(null).showAndWait();
        created.ifPresent(c -> {
            store.add(c);
            refresh();
        });
    }

    private void onEdit() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (!(selected instanceof Editable)) {
            return;
        }
        Optional<Contact> updated = new ContactDialog(selected).showAndWait();
        updated.ifPresent(c -> {
            if (!selected.getName().equals(c.getName())) {
                store.removeByKey(selected.getName());
            }
            store.add(c);
            refresh();
        });
    }

    private void onDelete() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        store.removeByKey(selected.getName());
        refresh();
    }

    private void onFind(String key) {
        if (key == null || key.isBlank()) {
            searchLabel.setText("Поиск: пустой запрос");
            refresh();
            return;
        }
        Contact found = store.findByKey(key);
        if (found != null) {
            searchLabel.setText("Поиск: найден: " + found.getName());
            table.getSelectionModel().select(found);
        } else {
            searchLabel.setText("Поиск: «" + key + "» не найден");
        }
        refresh(); // перерисовка с подсветкой пройденного пути
    }

    private void onAutocomplete(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        Contact[] hits = store.autocomplete(prefix);
        int count = store.countInSubtree(prefix);
        String first = Arrays.stream(hits).limit(5).map(Contact::getName)
                .collect(Collectors.joining(", "));
        prefixLabel.setText("Совпадений: " + count + (first.isEmpty() ? "" : " | напр.: " + first));
        refresh();
    }

    // ---- фоновая загрузка CSV через Task с прогрессом ----

    private void onLoad(Stage stage) {
        File file = csvChooser("Загрузить из CSV").showOpenDialog(stage);
        if (file == null) {
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                CsvLoadResult result = CsvLoader.load(file.toPath());
                List<Contact> list = result.getContacts();
                int total = list.size();
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    store.add(list.get(i));
                    if (i % 200 == 0 || i + 1 == total) {
                        updateProgress(i + 1, total);
                    }
                }
                Platform.runLater(() -> {
                    refresh();
                    if (result.hasErrors()) {
                        showSkippedDialog(result);
                    }
                });
                return null;
            }
        };
        runTask(task, "Загрузка CSV...");
    }

    // ---- фоновая генерация с отменой ----

    private void onGenerate(String text) {
        int n;
        try {
            n = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            showError("Генерация", "N должно быть целым числом");
            return;
        }
        if (n <= 0 || n > 200_000) {
            showError("Генерация", "N должно быть 1..200000");
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                for (int i = 0; i < n; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    store.add(ContactGenerator.make(i));
                    if (i % 500 == 0 || i + 1 == n) {
                        updateProgress(i + 1, n);
                    }
                }
                Platform.runLater(ContactApp.this::refresh);
                return null;
            }
        };
        runTask(task, "Генерация " + n + "...");
    }

    // ---- гонка в фоне ----

    private void onRace() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return TrieBenchmark.raceDemo(8, 2000);
            }
        };
        task.setOnSucceeded(e -> outArea.setText(task.getValue()));
        task.setOnFailed(e -> showError("Демо гонки", String.valueOf(task.getException())));
        runTask(task, "Демо гонки...");
    }

    private void runTask(Task<?> task, String status) {
        if (currentTask != null && currentTask.isRunning()) {
            showError("Фоновая задача", "Дождитесь завершения или нажмите «Отмена»");
            return;
        }
        currentTask = task;
        progress.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        task.setOnCancelled(e -> {
            progress.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progress.setProgress(0);
            refresh();
        });
        task.setOnSucceeded(e -> {
            progress.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progress.setProgress(1);
            refresh();
        });
        task.setOnFailed(e -> {
            progress.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            showError("Ошибка фоновой задачи", String.valueOf(task.getException()));
            refresh();
        });
        statusLabel.textProperty().unbind();
        statusLabel.setText(status);
        new Thread(task, "lab2-task").start();
    }

    // ---- общее ----

    /** Синхронизировать витрину таблицы и перерисовать Trie. */
    private void refresh() {
        Contact[] snap = store.snapshot();
        mirror.setAll(new ArrayList<>(Arrays.asList(snap)));
        if (canvas != null) {
            canvas.draw(store.rootForView(), store.lastPath());
        }
        if (statusLabel != null && (currentTask == null || !currentTask.isRunning())) {
            statusLabel.setText("Trie size: " + store.size());
        }
    }

    private void onSave(Stage stage) {
        File file = csvChooser("Сохранить в CSV").showSaveDialog(stage);
        if (file == null) {
            return;
        }
        java.nio.file.Path path = file.toPath();
        if (!path.toString().toLowerCase().endsWith(".csv")) {
            path = path.resolveSibling(path.getFileName() + ".csv");
        }
        try {
            CsvLoader.save(new ArrayList<>(Arrays.asList(store.snapshot())), path);
        } catch (IOException ex) {
            showError("Ошибка сохранения", ex.getMessage());
        }
    }

    private static TableColumn<Contact, String> typeColumn() {
        TableColumn<Contact, String> col = new TableColumn<>("Тип");
        col.setPrefWidth(100);
        col.setCellValueFactory(data -> {
            Contact c = data.getValue();
            if (c instanceof CorporateContact) {
                return new ReadOnlyStringWrapper("CORPORATE");
            }
            return new ReadOnlyStringWrapper(c.getClass().getSimpleName().toUpperCase());
        });
        return col;
    }

    private static TableColumn<Contact, String> column(String title, String property, int width) {
        TableColumn<Contact, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private FileChooser csvChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        return chooser;
    }

    private void showSkippedDialog(CsvLoadResult result) {
        String details = result.getErrors().stream()
                .map(CsvRowError::toString)
                .collect(Collectors.joining("\n"));
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Загрузка завершена с пропусками");
        alert.setHeaderText("Загружено: " + result.getContacts().size()
                + ", пропущено строк: " + result.getErrors().size());
        alert.setContentText(details);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
