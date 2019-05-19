package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class MemoryDB {

    private static volatile MemoryDB memoryDb;

    private List<Node> data;

    private MemoryDB() {
        data = new CopyOnWriteArrayList<>();
    }

    public static MemoryDB instance() {

        if (memoryDb == null) {
            memoryDb = new MemoryDB();
        }

        return memoryDb;
    }

    public void add(Node node) {
        final String key = UUID.randomUUID().toString().replace("-", "");
        node.setKey(key);
        data.add(node);
    }

    public void remove(String key) {

        synchronized (data) {
            data.removeIf(v -> v.getKey().equals(key));
        }
    }

    public List<Node> all() {
        return data;
    }

    public Node one(String key) {

        for (Node n : data) {

            if (n.getKey().equals(key)) {
                return n;
            }
        }

        return null;
    }

    public int size() {
        return this.data.size();
    }
}
