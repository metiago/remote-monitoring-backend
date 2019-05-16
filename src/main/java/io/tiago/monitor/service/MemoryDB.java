package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;

import java.util.LinkedList;
import java.util.List;

public class MemoryDB {

    private static volatile MemoryDB memoryDb;

    private List<Node> data;

    private MemoryDB() {
        data = new LinkedList<>();
    }

    public static MemoryDB instance() {

        if (memoryDb == null) {
            memoryDb = new MemoryDB();
        }

        return memoryDb;
    }

    public void add(Node node) {

        synchronized (data) {
            data.add(node);
        }
    }

    public void remove(String key) {

        synchronized (data) {

            // TODO Implement iterator removal
        }
    }

    public List<Node> all() {
        return data;
    }

    public int size() {
        return this.data == null ? 0 : this.data.size();
    }
}
