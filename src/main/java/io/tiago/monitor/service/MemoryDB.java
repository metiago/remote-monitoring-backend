package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryDB {

    private static volatile MemoryDB memoryDb;

    private Map<String, Node> data;

    private MemoryDB() {
        data = new LinkedHashMap<>();
    }

    public static MemoryDB instance() {

        if (memoryDb == null) {
            memoryDb = new MemoryDB();
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
