package io.tiago.monitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryDb {

    private static volatile MemoryDb memoryDb;

    private Map<String, Node> data;

    private MemoryDb() {
        data = new LinkedHashMap<>();
    }

    public static MemoryDb instance() {

        if (memoryDb == null) {
            memoryDb = new MemoryDb();
        }

        return memoryDb;
    }

    public void add(Node node) {

        synchronized (data) {
            String key = UUID.randomUUID().toString().replace("-", "");
            data.put(key, node);
        }
    }

    public void remove(String key) {

        synchronized (data) {
            data.remove(key);
        }
    }

    public Map<String, Node> all() {
        return data;
    }

    public int size() {
        return this.data == null ? 0 : this.data.size();
    }
}
