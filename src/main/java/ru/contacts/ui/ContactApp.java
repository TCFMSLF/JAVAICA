package ru.contacts.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

/**
 * Главное окно справочника контактов (Лаб. 1, вариант 4).
 * Таблица + кнопки «Загрузить CSV», «Сохранить CSV», «Добавить», «Изменить».
 * Кнопка «Изменить» активна только для Editable-типов.
 */
public class ContactApp extends Application {

    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private TableView<Contact> table;
    private Button editButton;

    @Override
    @SuppressWarnings("unchecked") // TableColumn varargs в getColumns().addAll
    public void start(Stage stage) {
        table = new TableView<>(contacts);
        table.setPrefHeight(400);
        table.getColumns().addAll(
                typeColumn(),
                column("Имя", "name", 160),
                column("Телефон", "phone", 140),
                column("E-mail", "email", 170),
                column("Организация", "organization", 140),
                column("Должность", "position", 120),
                column("Внутр. номер", "internalNumber", 100));

        Button loadButton = new Button("Загрузить CSV");
        Button saveButton = new Button("Сохранить CSV");
        Button addButton = new Button("Добавить");
        editButton = new Button("Изменить");
        editButton.setDisable(true);

        loadButton.setOnAction(e -> onLoad(stage));
        saveButton.setOnAction(e -> onSave(stage));
        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> editButton.setDisable(!(newSel instanceof Editable)));

        HBox buttons = new HBox(8, loadButton, saveButton, addButton, editButton);
        VBox root = new VBox(8, table, buttons);
        root.setPadding(new Insets(12));

        stage.setTitle("Справочник контактов");
        stage.setScene(new Scene(root, 900, 500));
        stage.show();
    }

    private static TableColumn<Contact, String> typeColumn() {
        TableColumn<Contact, String> col = new TableColumn<>("Тип");
        col.setPrefWidth(110);
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
        // У базовых типов свойств position/internalNumber нет — ячейка останется пустой.
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private FileChooser csvChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        return chooser;
    }

    private void onLoad(Stage stage) {
        File file = csvChooser("Загрузить из CSV").showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            CsvLoadResult result = CsvLoader.load(file.toPath());
            contacts.setAll(result.getContacts());
            if (result.hasErrors()) {
                showSkippedDialog(result);
            }
        } catch (IOException ex) {
            showError("Ошибка загрузки", ex.getMessage());
        }
    }

    private void onSave(Stage stage) {
        File file = csvChooser("Сохранить в CSV").showSaveDialog(stage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        if (!path.toString().toLowerCase().endsWith(".csv")) {
            path = path.resolveSibling(path.getFileName() + ".csv");
        }
        try {
            CsvLoader.save(new ArrayList<>(contacts), path);
        } catch (IOException ex) {
            showError("Ошибка сохранения", ex.getMessage());
        }
    }

    private void onAdd() {
        Optional<Contact> created = new ContactDialog(null).showAndWait();
        created.ifPresent(contacts::add);
    }

    private void onEdit() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (!(selected instanceof Editable)) {
            return;
        }
        Optional<Contact> updated = new ContactDialog(selected).showAndWait();
        updated.ifPresent(c -> contacts.set(contacts.indexOf(selected), c));
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
