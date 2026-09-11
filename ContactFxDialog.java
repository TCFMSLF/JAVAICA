import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Диалог JavaFX «Добавить» / «Редактировать».
 *
 * <ul>
 *   <li>«Добавить»: тип выбирается из {@link ContactFactory#ADDABLE_TYPES}
 *       (read-only {@code emergency} создать нельзя — его нет в списке).</li>
 *   <li>«Редактировать»: тип зафиксирован, меняются только поля.</li>
 *   <li>Ошибки валидации показываются диалогом с ошибкой, окно не закрывается.</li>
 * </ul>
 */
public final class ContactFxDialog extends Dialog<Contact> {

    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField orgField = new TextField();
    private final TextField positionField = new TextField();
    private final TextField internalField = new TextField();

    /** Диалог добавления. Пустой Optional = отмена. */
    public static Optional<Contact> showAdd(Window owner) {
        ContactFxDialog d = new ContactFxDialog(null);
        d.initOwner(owner);
        return d.showAndWait();
    }

    /** Диалог редактирования. Пустой Optional = отмена. */
    public static Optional<Contact> showEdit(Window owner, Contact original) {
        if (original instanceof EmergencyContact) {
            throw new UnsupportedOperationException("EmergencyContact is read-only");
        }
        ContactFxDialog d = new ContactFxDialog(original);
        d.initOwner(owner);
        return d.showAndWait();
    }

    private ContactFxDialog(Contact original) {
        boolean editMode = original != null;
        setTitle(editMode ? "Редактировать контакт" : "Добавить контакт");
        setHeaderText(null);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResultConverter(b -> b == ButtonType.OK ? buildResult(original) : null);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        int row = 0;
        if (!editMode) {
            typeBox.getItems().addAll(ContactFactory.ADDABLE_TYPES);
            typeBox.getSelectionModel().selectFirst();
            typeBox.valueProperty().addListener((o, a, b) -> updateCorporateFields(false));
            grid.add(new Label("Тип*:"), 0, row);
            grid.add(typeBox, 1, row++);
        } else {
            String t = original instanceof CorporateContact ? "corporate" : "contact";
            typeBox.getItems().add(t);
            typeBox.getSelectionModel().selectFirst();
            typeBox.setDisable(true);
            grid.add(new Label("Тип:"), 0, row);
            grid.add(new Label(t), 1, row++);
        }

        nameField.setPromptText("Обязательное");
        phoneField.setPromptText("Обязательный");
        grid.add(new Label("Имя*:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Телефон*:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("E-mail:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Организация:"), 0, row);
        grid.add(orgField, 1, row++);
        grid.add(new Label("Должность* (corporate):"), 0, row);
        grid.add(positionField, 1, row++);
        grid.add(new Label("Внутр. номер* (corporate):"), 0, row);
        grid.add(internalField, 1, row++);

        if (editMode) {
            nameField.setText(nn(original.getName()));
            phoneField.setText(nn(original.getPhone()));
            emailField.setText(nn(original.getEmail()));
            orgField.setText(nn(original.getOrganization()));
            if (original instanceof CorporateContact cc) {
                positionField.setText(nn(cc.getPosition()));
                internalField.setText(nn(cc.getInternalNumber()));
            }
        }
        updateCorporateFields(editMode && !(original instanceof CorporateContact));

        getDialogPane().setContent(grid);

        // Валидация до закрытия: при ошибке показываем Alert(ERROR) и блокируем закрытие.
        Button ok = (Button) getDialogPane().lookupButton(ButtonType.OK);
        ok.setText(editMode ? "Сохранить" : "Добавить");
        ok.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                buildResultOrThrow(original);
            } catch (IllegalArgumentException ex) {
                ContactFxApp.error("Ошибка данных:\n" + ex.getMessage());
                e.consume();
            }
        });
    }

    private void updateCorporateFields(boolean lockAsNonCorporate) {
        boolean isCorporate = !lockAsNonCorporate && "corporate".equals(typeBox.getValue());
        positionField.setDisable(!isCorporate);
        internalField.setDisable(!isCorporate);
    }

    private Contact buildResult(Contact original) {
        // Вызывается конвертером только если фильтр пропустил (данные валидны).
        return buildResultOrThrow(original);
    }

    private Contact buildResultOrThrow(Contact original) {
        String type = original != null
                ? (original instanceof CorporateContact ? "corporate" : "contact")
                : typeBox.getValue();
        return ContactFactory.create(type,
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                orgField.getText().trim(),
                positionField.getText().trim(),
                internalField.getText().trim());
    }

    private static String nn(String s) {
        return s == null ? "" : s;
    }
}
