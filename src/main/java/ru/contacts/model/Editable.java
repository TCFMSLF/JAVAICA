package ru.contacts.model;

import java.util.List;

// Контракт «сущность можно менять».
// Кнопка «Изменить» в GUI доступна только для объектов, реализующих Editable,
// поэтому проверка идёт через instanceof Editable, а не по наличию сеттеров.
public interface Editable {
    // Пустой список = данные корректны.
    List<String> validate();
}
