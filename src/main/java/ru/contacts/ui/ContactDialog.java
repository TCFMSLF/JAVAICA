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
import ru.contacts.model.CorporateContact;
import ru.contacts.model.Editable;
import ru.contacts.model.EmergencyContact;

/**
 * Диалог добавления/редактирования контакта.
 * В режиме добавления тип выбирается; CONTACT и EMERGENCY заблокированы
 * для создания (появляются только из файла) — создать можно только
 * редактируемый CorporateContact. В режиме редактирования тип фиксирован.
 * Сохранение блокируется, пока {@link Editable#validate()} возвращает ошибки.
 */
public class ContactDialog extends Dialog<Contact> {

    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField orgField = new TextField();
    private final TextField positionField = new TextField();
    private final TextField internalField = new TextField();
    private final Label hintLabel = new Label();

    /**
     * @param existing контакт для редактирования или null для добавления
     */
    public ContactDialog(Contact existing) {
        boolean editMode = existing != null;
        setTitle(editMode ? "Изменить контакт" : "Добавить контакт");

        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        typeBox.getItems().addAll("CONTACT", "EMERGENCY", "CORPORATE");
        if (editMode) {
            typeBox.setValue(typeOf(existing));
            typeBox.setDisable(true);
            fillFields(existing);
        } else {
            typeBox.setValue("CORPORATE");
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

        Runnable refresh = () -> {
            boolean corporate = "CORPORATE".equals(typeBox.getValue());
            boolean cont = "CONTACT".equals(typeBox.getValue());
            positionField.setDisable(!corporate);
            internalField.setDisable(!corporate);
            if (!editMode && !(corporate || cont)){
                hintLabel.setText("Тип " + typeBox.getValue()
                        + " появляется только из файла. Выберите CORPORATE.");
            } else {
                hintLabel.setText("");
            }
            getDialogPane().lookupButton(saveType)
                    .setDisable(!editMode && !(corporate || cont));
        };
        typeBox.setOnAction(e -> refresh.run());
        refresh.run();

        // Валидация до закрытия: при ошибках диалог остаётся открытым.
        getDialogPane().lookupButton(saveType).addEventFilter(ActionEvent.ACTION, event -> {
            Contact candidate = buildContact();
            if (candidate instanceof Editable) {
                List<String> errors = ((Editable) candidate).validate();
                if (!errors.isEmpty()) {
                    hintLabel.setText(String.join("; ", errors));
                    event.consume();
                }
            }
        });

        setResultConverter(button -> button == saveType ? buildContact() : null);
    }

    private void fillFields(Contact c) {
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail());
        orgField.setText(c.getOrganization());
        if (c instanceof CorporateContact) {
            positionField.setText(((CorporateContact) c).getPosition());
            internalField.setText(((CorporateContact) c).getInternalNumber());
        }
    }

    private Contact buildContact() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String org = orgField.getText().trim();
        switch (typeBox.getValue()) {
            case "EMERGENCY":
                return new EmergencyContact(name, phone, email, org);
            case "CORPORATE":
                return new CorporateContact(name, phone, email, org,
                        positionField.getText().trim(), internalField.getText().trim());
            default:
                return new Contact(name, phone, email, org);
        }
    }

    private static String typeOf(Contact c) {
        if (c instanceof CorporateContact) {
            return "CORPORATE";
        }
        if (c instanceof EmergencyContact) {
            return "EMERGENCY";
        }
        return "CONTACT";
    }
}
