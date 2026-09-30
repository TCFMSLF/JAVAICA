package ru.contacts.store;

/**
 * Собственный узел Trie.
 * Дети хранятся двумя параллельными массивами (char + узел),
 * без java.util. Поиск ребёнка — линейный скан.
 *
 * @param <V> тип значения в терминальном узле
 */
public final class TrieNode<V> {
    public final char ch;
    public V value;
    public boolean terminal;
    /** Число терминальных записей в поддереве с этим узлом (включая сам узел). */
    public int subtreeCount;
    public char[] keys = new char[0];
    @SuppressWarnings("unchecked")
    public TrieNode<V>[] children = (TrieNode<V>[]) new TrieNode[0];
    public int childCount = 0;

    public TrieNode(char ch) {
        this.ch = ch;
    }

    public TrieNode<V> findChild(char c) {
        for (int i = 0; i < childCount; i++) {
            if (keys[i] == c) {
                return children[i];
            }
        }
        return null;
    }

    public int indexOfChild(char c) {
        for (int i = 0; i < childCount; i++) {
            if (keys[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public void addChild(TrieNode<V> node) {
        if (childCount == keys.length) {
            int next = keys.length == 0 ? 2 : keys.length * 2;
            char[] nk = new char[next];
            @SuppressWarnings("unchecked")
            TrieNode<V>[] nc = (TrieNode<V>[]) new TrieNode[next];
            for (int i = 0; i < childCount; i++) {
                nk[i] = keys[i];
                nc[i] = children[i];
            }
            keys = nk;
            children = nc;
        }
        keys[childCount] = node.ch;
        children[childCount] = node;
        childCount++;
    }

    public void removeChildAt(int idx) {
        for (int i = idx; i < childCount - 1; i++) {
            keys[i] = keys[i + 1];
            children[i] = children[i + 1];
        }
        childCount--;
        keys[childCount] = 0;
        children[childCount] = null;
    }
}
