package ru.contacts.store;

/**
 * Минимальный контракт хранилища (Лаб. 2).
 * НЕ дженерик по сущности в исходном примере был BookStore,
 * здесь обобщённый вариант: ключ K, сущность T.
 */
public interface EntityStore<K, T> {
    /** Вставка (дубликат ключа перезаписывает значение). */
    void add(T entity);

    /** Точный поиск по ключу, null если нет. */
    T findByKey(K key);

    /** Удаление по ключу, true если запись была. */
    boolean removeByKey(K key);

    /** Число записей. */
    int size();
}
