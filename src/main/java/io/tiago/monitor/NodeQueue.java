package io.tiago.monitor;

import java.util.ArrayList;
import java.util.List;

public class NodeQueue {

    private static volatile NodeQueue nodeQueue;

    private List<Integer> nodes;

    private NodeQueue() {
        this.nodes = new ArrayList<>();
    }

    public static NodeQueue instance() {

        if (nodeQueue == null) {
            nodeQueue = new NodeQueue();
        }

        return nodeQueue;
    }

    public void add(Node node) {

        synchronized (nodes) {
            this.nodes.add(node.getPort());
        }
    }

    public int size() {
        return this.nodes == null ? 0 : this.nodes.size();
    }
}
