package io.tiago.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton pattern to control how many node could be added to be monitored
 */
public class NodeQueue {

    private static NodeQueue nodeQueue;

    private List<Integer> nodes = new ArrayList<>();

    private NodeQueue() {
    }

    public static NodeQueue instance() {

        if (nodeQueue == null) {
            nodeQueue = new NodeQueue();
        }

        return nodeQueue;
    }

    public void add(Node node) {
        nodes.add(node.getPort());
    }

    public int size() {
        return this.nodes == null ? 0 : this.nodes.size();
    }
}
