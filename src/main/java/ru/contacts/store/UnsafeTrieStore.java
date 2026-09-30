package ru.contacts.store;

/**
 * Та же логика, что у {@link TrieStore}, но БЕЗ синхронизации.
 * Используется только для демонстрации гонки: параллельная запись
 * из нескольких потоков теряет обновления (size и структура).
 * В основном коде не используется.
 */
public class UnsafeTrieStore<V> {

    private final KeyExtractor<V> extractor;
    private final TrieNode<V> root = new TrieNode<>('\0');
    private int size = 0;

    public UnsafeTrieStore(KeyExtractor<V> extractor) {
        if (extractor == null) {
            throw new IllegalArgumentException("extractor обязателен");
        }
        this.extractor = extractor;
    }

    public void add(V entity) {
        put(extractor.keyOf(entity), entity);
    }

    public void put(String key, V value) {
        TrieNode<V> node = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            TrieNode<V> next = node.findChild(c);
            if (next == null) {
                next = new TrieNode<>(c);
                node.addChild(next);
            }
            node = next;
        }
        if (!node.terminal) {
            node.terminal = true;
            node.value = value;
            size++; // гонка: инкремент не атомарен, обновления теряются
        } else {
            node.value = value;
        }
    }

    public int size() {
        return size;
    }
}
