package ru.contacts.ui;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.contacts.model.Contact;
import ru.contacts.model.ContactType;
import ru.contacts.model.CorporateContact;

// Диалог добавления/редактирования корпоративного контакта.
//
// Через GUI можно создавать и редактировать только редактируемые типы, а read-only
// появляются исключительно при загрузке из файла. Единственный редактируемый тип
// варианта 4 — CorporateContact, поэтому диалог работает только с ним.
//
// Сохранение блокируется, пока validate() возвращает ошибки.
public class ContactDialog extends Dialog<Contact> {

    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField orgField = new TextField();
    private final TextField positionField = new TextField();
    private final TextField internalField = new TextField();
    private final Label hintLabel = new Label();

    public ContactDialog(CorporateContact existing) {
        boolean editMode = existing != null;
        setTitle(editMode ? "Изменить контакт" : "Добавить контакт");

        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // Значение одно и выбор заблокирован: создавать можно только редактируемые типы,
        // но ограничение лучше показать, чем скрыть.
        typeBox.getItems().add(ContactType.CORPORATE.tag());
        typeBox.setValue(ContactType.CORPORATE.tag());
        typeBox.setDisable(true);
        if (editMode) {
            fillFields(existing);
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.add(new Label("Тип:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Имя:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Телефон:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Организация:"), 0, 4);
        grid.add(orgField, 1, 4);
        grid.add(new Label("Должность:"), 0, 5);
        grid.add(positionField, 1, 5);
        grid.add(new Label("Внутр. номер:"), 0, 6);
        grid.add(internalField, 1, 6);
        grid.add(hintLabel, 0, 7, 2, 1);
        getDialogPane().setContent(grid);

        // Валидация до закрытия: при ошибках диалог остаётся открытым.
        // Именно consume, а не просто return: без отмены события диалог закроется
        // с незаполненными полями.
        getDialogPane().lookupButton(saveType).addEventFilter(ActionEvent.ACTION, event -> {
            List<String> errors = buildContact().validate();
            if (!errors.isEmpty()) {
                hintLabel.setText(String.join("; ", errors));
                event.consume();
            }
        });

        setResultConverter(button -> button == saveType ? buildContact() : null);
    }

    private void fillFields(CorporateContact c) {
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail());
        orgField.setText(c.getOrganization());
        positionField.setText(c.getPosition());
        internalField.setText(c.getInternalNumber());
    }

    private Contact buildContact() {
        return new CorporateContact(
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                orgField.getText().trim(),
                positionField.getText().trim(),
                internalField.getText().trim());
    }
}
