package ru.contacts.store;

import ru.contacts.model.Contact;

/**
 * Демонстрация конкурентного доступа: параллельная запись
 * в незащищённую структуру против защищённой ({@code synchronized}).
 */
public final class TrieBenchmark {

    private TrieBenchmark() {
    }

    /** Демо гонки: параллельная запись без защиты vs с synchronized. */
    public static String raceDemo(int threads, int perThread) {
        StringBuilder sb = new StringBuilder();
        int expected = threads * perThread;

        // незащищённая структура
        UnsafeTrieStore<Contact> unsafe = new UnsafeTrieStore<>(Contact::getName);
        Thread[] ts = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int base = t * perThread;
            ts[t] = new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    unsafe.add(ContactGenerator.make(base + i));
                }
            });
        }
        long t0 = System.nanoTime();
        for (Thread th : ts) {
            th.start();
        }
        for (Thread th : ts) {
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long unsafeMs = (System.nanoTime() - t0) / 1_000_000;
        sb.append("Без защиты (UnsafeTrieStore): size=").append(unsafe.size())
                .append(" из ").append(expected)
                .append(", время ").append(unsafeMs).append(" мс");
        if (unsafe.size() != expected) {
            sb.append(" — ГОНКА: обновления потеряны");
        } else {
            sb.append(" — в этот раз повезло, повторите (гонка недетерминирована)");
        }
        sb.append('\n');

        // защищённая структура
        TrieStore<Contact> safe = ContactStores.newStore();
        Thread[] ts2 = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int base = 1_000_000 + t * perThread;
            ts2[t] = new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    safe.add(ContactGenerator.make(base + i));
                }
            });
        }
        t0 = System.nanoTime();
        for (Thread th : ts2) {
            th.start();
        }
        for (Thread th : ts2) {
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long safeMs = (System.nanoTime() - t0) / 1_000_000;
        sb.append("С защитой (TrieStore, synchronized): size=").append(safe.size())
                .append(" из ").append(expected)
                .append(", время ").append(safeMs).append(" мс — корректно");
        return sb.toString();
    }
}
