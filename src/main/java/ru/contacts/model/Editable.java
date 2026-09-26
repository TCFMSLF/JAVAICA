package ru.contacts.model;

import java.util.List;

/**
 * Контракт «сущность можно менять».
 * Кнопка «Изменить» в GUI доступна только для объектов, реализующих Editable.
 */
public interface Editable {
    /**
     * @return список ошибок; пустой список = данные корректны
     */
    List<String> validate();
}
