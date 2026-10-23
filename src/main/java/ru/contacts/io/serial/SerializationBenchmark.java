package ru.contacts.io.serial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import ru.contacts.model.Contact;
import ru.contacts.store.ContactGenerator;

/**
 * Сравнение JSON и бинарного форматов на одинаковом наборе контактов.
 * В отчёт попадают размер файла и средние времена сохранения/загрузки
 * по заданному числу прогонов (после прогревочного прогона).
 */
public final class SerializationBenchmark {

    private static final int WARMUP_RUNS = 3;

    private SerializationBenchmark() {
    }

    public static String compare(int count, int runs) throws IOException {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть > 0");
        }
        if (runs <= 0) {
            throw new IllegalArgumentException("runs должен быть > 0");
        }
        Contact[] contacts = new Contact[count];
        for (int i = 0; i < count; i++) {
            contacts[i] = ContactGenerator.make(i);
        }

        StringBuilder report = new StringBuilder();
        report.append("Замеры сериализации: ").append(count)
                .append(" контактов, ").append(runs)
                .append(" прогонов (после прогрева)\n\n");
        compareOne(report, "JSON (Jackson)", new JsonContactSerializer(), contacts, runs);
        report.append('\n');
        compareOne(report, "BIN (Java Serializable)", new BinaryContactSerializer(), contacts, runs);
        return report.toString();
    }

    private static <T> void compareOne(StringBuilder report, String label, Serializer<T> serializer,
                                       T data, int runs) throws IOException {
        Path file = Files.createTempFile("contacts-bench-", "." + serializer.format());
        try {
            // прогрев: исключаем влияние загрузки классов и JIT на средние значения
            for (int i = 0; i < WARMUP_RUNS; i++) {
                serializer.save(data, file);
                serializer.load(file);
            }

            long saveNanos = 0;
            long loadNanos = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                serializer.save(data, file);
                saveNanos += System.nanoTime() - start;

                start = System.nanoTime();
                serializer.load(file);
                loadNanos += System.nanoTime() - start;
            }
            long bytes = Files.size(file);

            report.append(label).append('\n');
            report.append("  Размер файла: ").append(bytes).append(" байт (")
                    .append(String.format(Locale.ROOT, "%.2f", bytes / 1024.0 / 1024.0))
                    .append(" МиБ)\n");
            report.append("  Сохранение:   ")
                    .append(String.format(Locale.ROOT, "%.2f", saveNanos / 1_000_000.0 / runs))
                    .append(" мс (среднее)\n");
            report.append("  Загрузка:     ")
                    .append(String.format(Locale.ROOT, "%.2f", loadNanos / 1_000_000.0 / runs))
                    .append(" мс (среднее)\n");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
