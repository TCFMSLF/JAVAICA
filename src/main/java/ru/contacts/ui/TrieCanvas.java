package ru.contacts.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.contacts.store.TrieNode;

/**
 * Панель визуализации Trie: внутреннее устройство структуры.
 * Полная перерисовка после каждой операции; узлы последнего
 * поиска/вставки подсвечены оранжевым, терминальные — зелёным.
 */
public class TrieCanvas extends Canvas {

    private static final int MAX_NODES = 500;
    private static final double R = 12;
    private static final double V_GAP = 48;
    private static final double TOP = 26;

    public TrieCanvas(double width, double height) {
        super(width, height);
    }

    /** Полная перерисовка панели. */
    public void draw(TrieNode<?> root, TrieNode<?>[] highlight) {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        g.clearRect(0, 0, w, h);
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setFont(Font.font(11));

        if (root == null || (root.childCount == 0 && !root.terminal)) {
            g.setFill(Color.GRAY);
            g.fillText("Trie пусто — загрузите CSV или сгенерируйте данные", 12, 24);
            return;
        }

        // BFS: уровни + родители, с ограничением
        List<List<TrieNode<?>>> levels = new ArrayList<>();
        List<TrieNode<?>> order = new ArrayList<>();
        Map<TrieNode<?>, TrieNode<?>> parent = new HashMap<>();
        Deque<TrieNode<?>> q = new ArrayDeque<>();
        Deque<Integer> d = new ArrayDeque<>();
        q.add(root);
        d.add(0);
        parent.put(root, null);
        int total = countNodes(root);
        while (!q.isEmpty() && order.size() < MAX_NODES) {
            TrieNode<?> n = q.poll();
            int depth = d.poll();
            while (levels.size() <= depth) {
                levels.add(new ArrayList<>());
            }
            levels.get(depth).add(n);
            order.add(n);
            for (int i = 0; i < n.childCount; i++) {
                TrieNode<?> c = n.children[i];
                parent.put(c, n);
                q.add(c);
                d.add(depth + 1);
            }
        }

        Map<TrieNode<?>, double[]> pos = new HashMap<>();
        for (int depth = 0; depth < levels.size(); depth++) {
            List<TrieNode<?>> level = levels.get(depth);
            for (int i = 0; i < level.size(); i++) {
                double x = w * (i + 1.0) / (level.size() + 1.0);
                double y = TOP + depth * V_GAP;
                pos.put(level.get(i), new double[]{x, y});
            }
        }

        Map<TrieNode<?>, Boolean> hot = new HashMap<>();
        if (highlight != null) {
            for (TrieNode<?> n : highlight) {
                if (n != null) {
                    hot.put(n, Boolean.TRUE);
                }
            }
        }

        // рёбра
        g.setStroke(Color.LIGHTGRAY);
        for (TrieNode<?> n : order) {
            double[] p = pos.get(n);
            if (p == null) {
                continue;
            }
            TrieNode<?> par = parent.get(n);
            if (par != null && pos.containsKey(par)) {
                double[] pp = pos.get(par);
                g.strokeLine(pp[0], pp[1], p[0], p[1]);
            }
        }

        // узлы
        for (TrieNode<?> n : order) {
            double[] p = pos.get(n);
            if (p == null) {
                continue;
            }
            double x = p[0];
            double y = p[1];
            boolean isHot = hot.containsKey(n);
            if (isHot) {
                g.setFill(Color.ORANGE);
            } else if (n.terminal) {
                g.setFill(Color.LIGHTGREEN);
            } else {
                g.setFill(Color.LIGHTBLUE);
            }
            g.fillOval(x - R, y - R, 2 * R, 2 * R);
            g.setStroke(isHot ? Color.DARKORANGE : Color.DARKGRAY);
            g.strokeOval(x - R, y - R, 2 * R, 2 * R);
            g.setFill(Color.BLACK);
            String label = n == root ? "root" : String.valueOf(n.ch);
            g.fillText(label, x - 4, y + 4);
            if (n.terminal) {
                g.setFill(Color.DARKGREEN);
                g.fillText("•", x + R - 2, y - R + 2);
            }
        }

        if (total > order.size()) {
            g.setFill(Color.GRAY);
            g.fillText("Показано " + order.size() + " из " + total + " узлов", 12, h - 10);
        } else {
            g.setFill(Color.GRAY);
            g.fillText("Узлов: " + total, 12, h - 10);
        }
    }

    private static int countNodes(TrieNode<?> root) {
        int cnt = 0;
        Deque<TrieNode<?>> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            TrieNode<?> n = q.poll();
            cnt++;
            for (int i = 0; i < n.childCount; i++) {
                q.add(n.children[i]);
            }
        }
        return cnt;
    }
}
