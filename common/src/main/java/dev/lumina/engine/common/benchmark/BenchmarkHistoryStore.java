package dev.lumina.engine.common.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class BenchmarkHistoryStore {
    public static final int MAX_RECORDS = 20;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;

    public BenchmarkHistoryStore(Path path) { this.path = path; }

    public List<BenchmarkRecord> load() throws IOException {
        if (Files.notExists(path)) return List.of();
        try {
            StoredHistory stored = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), StoredHistory.class);
            if (stored == null || stored.records == null) return List.of();
            return List.copyOf(stored.records);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    public List<BenchmarkRecord> append(BenchmarkRecord record) throws IOException {
        ArrayList<BenchmarkRecord> records = new ArrayList<>(load());
        records.add(record);
        if (records.size() > MAX_RECORDS) records.subList(0, records.size() - MAX_RECORDS).clear();
        save(records);
        return List.copyOf(records);
    }

    private void save(List<BenchmarkRecord> records) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(new StoredHistory(records)) + System.lineSeparator(), StandardCharsets.UTF_8);
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private record StoredHistory(List<BenchmarkRecord> records) {}
}
