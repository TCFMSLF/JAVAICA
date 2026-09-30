package ru.contacts.store;

import java.lang.reflect.Array;

/**
 * Собственная структура данных варианта 4: префиксное дерево (Trie) по строке-ключу.
 * Generic по значению: {@code TrieStore<Contact>} через {@link KeyExtractor}.
 *
 * <p>Хранение — только собственные узлы {@link TrieNode} и массивы,
 * {@code java.util} внутри не используется. Все публичные методы
 * {@code synchronized} — защита от гонки между FX-потоком и фоновыми Task.</p>
 *
 * <p>Асимптотика (L — длина ключа, P — размер поддерева префикса):
 * вставка O(L), поиск O(L), удаление O(L), countInSubtree O(L),
 * autocomplete O(L + P).</p>
 *
 * @param <V> тип хранимого значения
 */
public class TrieStore<V> implements EntityStore<String, V> {

    private final KeyExtractor<V> extractor;
    private final Class<V> valueType;
    private final TrieNode<V> root = new TrieNode<>('\0');
    private int size = 0;

    /** Путь последнего поиска/вставки для подсветки в визуализации (копия). */
    private TrieNode<V>[] lastPath = emptyNodes();

    public TrieStore(KeyExtractor<V> extractor, Class<V> valueType) {
        if (extractor == null || valueType == null) {
            throw new IllegalArgumentException("extractor и valueType обязательны");
        }
        this.extractor = extractor;
        this.valueType = valueType;
    }

    @Override
    public synchronized void add(V entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity == null");
        }
        String key = extractor.keyOf(entity);
        put(key, entity);
    }

    /** Вставка по явному ключу (дубликат перезаписывает значение, size не растёт). */
    public synchronized void put(String key, V value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("ключ не должен быть пустым");
        }
        if (value == null) {
            throw new IllegalArgumentException("value == null");
        }
        TrieNode<V> node = root;
        TrieNode<V>[] path = newNodes(key.length() + 1);
        path[0] = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            TrieNode<V> next = node.findChild(c);
            if (next == null) {
                next = new TrieNode<>(c);
                node.addChild(next);
            }
            node = next;
            path[i + 1] = node;
        }
        if (!node.terminal) {
            node.terminal = true;
            node.value = value;
            size++;
            for (TrieNode<V> n : path) {
                n.subtreeCount++;
            }
        } else {
            node.value = value; // перезапись дубликата
        }
        lastPath = path;
    }

    @Override
    public synchronized V findByKey(String key) {
        if (key == null || key.isEmpty()) {
            lastPath = emptyNodes();
            return null;
        }
        TrieNode<V> node = root;
        TrieNode<V>[] path = newNodes(key.length() + 1);
        path[0] = root;
        for (int i = 0; i < key.length(); i++) {
            node = node.findChild(key.charAt(i));
            if (node == null) {
                // подсветить пройденную часть пути
                TrieNode<V>[] cut = newNodes(i + 1);
                for (int j = 0; j <= i; j++) {
                    cut[j] = path[j];
                }
                lastPath = cut;
                return null;
            }
            path[i + 1] = node;
        }
        lastPath = path;
        return node.terminal ? node.value : null;
    }

    @Override
    public synchronized boolean removeByKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        TrieNode<V>[] path = newNodes(key.length() + 1);
        path[0] = root;
        TrieNode<V> node = root;
        for (int i = 0; i < key.length(); i++) {
            node = node.findChild(key.charAt(i));
            if (node == null) {
                lastPath = pathCopy(path, i + 1);
                return false;
            }
            path[i + 1] = node;
        }
        if (!node.terminal) {
            lastPath = path;
            return false;
        }
        node.terminal = false;
        node.value = null;
        size--;
        for (TrieNode<V> n : path) {
            n.subtreeCount--;
        }
        // чистка пустых листьев снизу вверх
        for (int i = key.length(); i >= 1; i--) {
            TrieNode<V> cur = path[i];
            TrieNode<V> parent = path[i - 1];
            if (cur.terminal || cur.childCount > 0) {
                break;
            }
            int idx = parent.indexOfChild(cur.ch);
            if (idx >= 0) {
                parent.removeChildAt(idx);
            }
        }
        lastPath = pathCopy(path, key.length()); // подсветить предков
        return true;
    }

    @Override
    public synchronized int size() {
        return size;
    }

    public synchronized void clear() {
        root.keys = new char[0];
        @SuppressWarnings("unchecked")
        TrieNode<V>[] none = (TrieNode<V>[]) new TrieNode[0];
        root.children = none;
        root.childCount = 0;
        root.subtreeCount = 0;
        root.terminal = false;
        root.value = null;
        size = 0;
        lastPath = emptyNodes();
    }

    /** Автодополнение: все значения с данным префиксом. O(L + P). */
    public synchronized V[] autocomplete(String prefix) {
        TrieNode<V> node = navigate(prefix);
        if (node == null) {
            return newArray(0);
        }
        Collector col = new Collector(node.subtreeCount);
        collect(node, col);
        return col.toArray();
    }

    /** Число контактов в поддереве префикса. O(L). */
    public synchronized int countInSubtree(String prefix) {
        TrieNode<V> node = navigate(prefix);
        return node == null ? 0 : node.subtreeCount;
    }

    /** Неизменяемый снимок всех значений (для таблицы/отрисовки из FX-потока). */
    public synchronized V[] snapshot() {
        Collector col = new Collector(size);
        collect(root, col);
        return col.toArray();
    }

    /** Корень для отрисовки (читать только внутри synchronized блока вызывателя нельзя — возвращается как есть для Canvas). */
    public TrieNode<V> rootForView() {
        return root;
    }

    /** Копия пути последней операции для подсветки. */
    public synchronized TrieNode<V>[] lastPath() {
        TrieNode<V>[] copy = newNodes(lastPath.length);
        for (int i = 0; i < lastPath.length; i++) {
            copy[i] = lastPath[i];
        }
        return copy;
    }

    // ---- внутреннее ----

    private TrieNode<V> navigate(String prefix) {
        if (prefix == null) {
            return null;
        }
        TrieNode<V> node = root;
        TrieNode<V>[] path = newNodes(prefix.length() + 1);
        path[0] = root;
        for (int i = 0; i < prefix.length(); i++) {
            node = node.findChild(prefix.charAt(i));
            if (node == null) {
                lastPath = pathCopy(path, i + 1);
                return null;
            }
            path[i + 1] = node;
        }
        lastPath = path;
        return node;
    }

    private void collect(TrieNode<V> node, Collector col) {
        if (node.terminal) {
            col.add(node.value);
        }
        for (int i = 0; i < node.childCount; i++) {
            collect(node.children[i], col);
        }
    }

    @SuppressWarnings("unchecked")
    private TrieNode<V>[] newNodes(int n) {
        return (TrieNode<V>[]) new TrieNode[n];
    }

    private TrieNode<V>[] emptyNodes() {
        TrieNode<V>[] p = newNodes(1);
        p[0] = root;
        return p;
    }

    private TrieNode<V>[] pathCopy(TrieNode<V>[] src, int len) {
        TrieNode<V>[] c = newNodes(len);
        for (int i = 0; i < len && i < src.length; i++) {
            c[i] = src[i];
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    private V[] newArray(int n) {
        return (V[]) Array.newInstance(valueType, n);
    }

    /** Свой динамический массив без java.util. */
    private final class Collector {
        private V[] buf;
        private int n = 0;

        Collector(int capacity) {
            buf = newArray(Math.max(8, capacity));
        }

        void add(V v) {
            if (n == buf.length) {
                V[] next = newArray(buf.length * 2 + 1);
                for (int i = 0; i < n; i++) {
                    next[i] = buf[i];
                }
                buf = next;
            }
            buf[n++] = v;
        }

        V[] toArray() {
            V[] out = newArray(n);
            for (int i = 0; i < n; i++) {
                out[i] = buf[i];
            }
            return out;
        }
    }
}
