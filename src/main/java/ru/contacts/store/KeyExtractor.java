package ru.contacts.store;

/**
 * Извлекает строковый ключ из значения.
 * Нужен, чтобы {@link TrieStore} оставался generic:
 * дерево работает со строкой, а как её получить из V — решает вызыватель.
 */
public interface KeyExtractor<V> {
    String keyOf(V value);
}
