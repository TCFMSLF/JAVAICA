import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Префиксное дерево (Trie) по имени контакта.
 *
 * Дополнительные операции:
 *  - автодополнение по префиксу (все контакты в поддереве),
 *  - подсчёт контактов в поддереве.
 *
 * Поиск регистронезависимый: ключи приводятся к нижнему регистру,
 * оригинальные объекты {@link Contact} хранятся в листьях без изменений.
 */
public class ContactTrie {

    /** Узел дерева. */
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        List<Contact> contacts = new ArrayList<>(); // контакты, чьё имя заканчивается в этом узле
        int subtreeCount = 0; // сколько контактов в поддереве этого узла (включая сам узел)
    }

    private final Node root = new Node();

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /** Вставить контакт в дерево (ключ — имя контакта). */
    public void insert(Contact contact) {
        if (contact == null || contact.getName() == null) {
            return;
        }
        String key = normalize(contact.getName());
        Node node = root;
        node.subtreeCount++; // корень считает все контакты
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            node = node.children.computeIfAbsent(c, k -> new Node());
            node.subtreeCount++;
        }
        node.contacts.add(contact);
    }

    /** Найти узел, соответствующий префиксу; null если префикса нет. */
    private Node findNode(String prefix) {
        String key = normalize(prefix);
        Node node = root;
        for (int i = 0; i < key.length(); i++) {
            node = node.children.get(key.charAt(i));
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    /**
     * Автодополнение по префиксу: все контакты,
     * чьи имена начинаются с prefix.
     */
    public List<Contact> autocomplete(String prefix) {
        List<Contact> result = new ArrayList<>();
        Node node = findNode(prefix);
        if (node == null) {
            return result;
        }
        collect(node, result);
        return result;
    }

    private void collect(Node node, List<Contact> result) {
        result.addAll(node.contacts);
        for (Node child : node.children.values()) {
            collect(child, result);
        }
    }

    /**
     * Подсчёт контактов в поддереве префикса:
     * сколько контактов имеют имя с данным началом.
     * Работает за O(|prefix|) благодаря счётчикам в узлах.
     */
    public int countByPrefix(String prefix) {
        Node node = findNode(prefix);
        return node == null ? 0 : node.subtreeCount;
    }

    /** Общее число контактов в дереве. */
    public int size() {
        return root.subtreeCount;
    }
}
