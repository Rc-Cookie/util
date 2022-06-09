package com.github.rccookie.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class Graph<N,W> {

    private final Set<Node<N,W>> nodes = new HashSet<>();
    private final Set<Node<N,W>> nodesView = Collections.unmodifiableSet(nodes);


    public Set<Node<N, W>> getNodes() {
        return nodesView;
    }

    public void add(Node<N,W> node) {
        node.setGraph(this);
    }

    public void addLocal(Node<N,W> node) {
        node.setGraphLocal(this);
    }

    public void remove(Node<N,W> node) {
        if(node.graph == this)
            node.setGraphLocal(null);
    }


    public static class Node<N,W> implements Iterable<Node<N,W>> {

        private N value;
        private final Map<Node<N,W>, W> adjacent = new HashMap<>();

        private Graph<N,W> graph = null;

        public Node(N value) {
            this.value = value;
        }

        public Node(N value, Map<? extends Node<N,W>, ? extends W> adjacent) {
            this(value);
            this.adjacent.putAll(adjacent);
        }

        public N getValue() {
            return value;
        }

        public Map<Node<N,W>, W> getAdjacent() {
            return adjacent;
        }

        public Set<Node<N,W>> getAdjacentNodes() {
            return adjacent.keySet(); // Cached by HashMap
        }

        public Graph<N, W> getGraph() {
            return graph;
        }

        public void setValue(N value) {
            this.value = value;
        }

        public void connect(Node<N,W> node, W weight) {
            adjacent.put(node, weight);
            node.setGraph(graph);
        }

        public void connectAll(Map<? extends Node<N,W>, ? extends W> adjacent) {
            adjacent.forEach(this::connect);
        }

        public void biConnect(Node<N,W> node, W weight) {
            connect(node, weight);
            node.connect(this, weight);
        }

        public void biConnectAll(Map<? extends Node<N,W>, ? extends W> adjacent) {
            adjacent.forEach(this::biConnect);
        }

        public void disconnect(Object node) {
            //noinspection SuspiciousMethodCalls
            adjacent.remove(node);
        }

        public void disconnectAll(Set<?> nodes) {
            for(Object node : nodes)
                disconnect(node);
        }

        public void disconnectAll() {
            adjacent.clear();
        }

        public void biDisconnect(Node<?,?> node) {
            node.disconnect(this);
            disconnect(node);
        }

        public void biDisconnectAll(Set<? extends Node<?,?>> nodes) {
            for(var node : nodes)
                biDisconnect(node);
        }

        public void biDisconnectAll() {
            for(var node : getAdjacentNodes())
                node.disconnect(this);
            disconnectAll();
        }

        public void setGraph(Graph<N,W> graph) {
            if(this.graph == graph) return;
            for(var node : this) {
                if(this.graph != null)
                    this.graph.nodes.remove(node);
                node.graph = graph;
                if(graph != null)
                    graph.nodes.add(node);
            }
            if(this.graph != null)
                this.graph.nodes.remove(this);
            this.graph = graph;
            if(graph != null)
                graph.nodes.add(this);
        }

        public void setGraphLocal(Graph<N,W> graph) {
            if(this.graph == graph) return;
            disconnectAll();
            if(this.graph != null)
                this.graph.nodes.remove(this);
            this.graph = graph;
            if(graph != null)
                graph.nodes.add(this);
        }

        @NotNull
        @Override
        public Iterator<Node<N,W>> iterator() {
            Set<Object> visited = new HashSet<>();
            visited.add(this);
            return iterator(visited);
        }

        private Iterator<Node<N,W>> iterator(Set<Object> visited) {
            if(visited.contains(this))
                return Utils.emptyIterator();

            return new Iterator<>() {
                final Iterator<Node<N,W>> adjacent = getAdjacentNodes().iterator();
                Node<N,W> next;
                Iterator<Node<N,W>> it;
                {
                    updateNext();
                }

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Node<N, W> next() {
                    if(next == null)
                        throw new EmptyIteratorException();

                    Node<N,W> current = next;
                    updateNext();
                    return current;
                }

                private void updateNext() {
                    do next = getAnyNext();
                    while(visited.contains(next));
                    visited.add(next);
                }

                private Node<N,W> getAnyNext() {
                    if(it != null && it.hasNext())
                        return it.next(); // Currently iterated node has more child nodes

                    if(!adjacent.hasNext())
                        return null; // Current node has no more child nodes, and it was the last node

                    Node<N,W> next = adjacent.next(); // First iterate over adjacent node, then over its children
                    it = next.iterator(visited);
                    return next;
                }
            };
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Graph<Integer, Integer> graph = new Graph<>();
        Node<Integer, Integer>[] nodes = new Node[] {
                new Node<>(0),
                new Node<>(1),
                new Node<>(2),
                new Node<>(3),
                new Node<>(4),
                new Node<>(5)
        };
        nodes[0].biConnect(nodes[1], 1);
        nodes[1].connect(nodes[2], 12);
        nodes[0].connect(nodes[3], 3);
        nodes[3].biConnect(nodes[4], 34);
        nodes[4].connect(nodes[0], 40);
        nodes[3].biConnect(nodes[5], 35);
        nodes[4].biConnect(nodes[5], 45);

        nodes[0].setGraph(graph);

        Console.info(graph.getNodes());
        nodes[0].forEach(Console::info);
    }
}
