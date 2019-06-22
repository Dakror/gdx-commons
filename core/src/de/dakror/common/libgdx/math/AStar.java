package de.dakror.common.libgdx.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

public class AStar<T> {
    private class Node {
        private Node parent;
        private float g;
        private float h;
        private T data;

        Node(T data, Node parent) {
            this.data = data;
            setParent(parent);
        }

        void setParent(Node parent) {
            this.parent = parent;
            if (parent == null) g = 0;
            else g = parent.g + network.getEdgeLength(parent.data, data);
        }

        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass()) && hashCode() == obj.hashCode();
        }

        @Override
        public String toString() {
            return String.format("{%s, g=%f, h=%f}", data.toString(), g, h);
        }
    }

    public abstract static class Network<T> {
        public abstract float getH(T start, T end);

        public abstract float getEdgeLength(T start, T end);

        public abstract void visitNeighbors(T node, Consumer<T> visitor);
    }

    LinkedList<Node> openList;
    HashSet<Node> closedList;
    Network<T> network;
    T finish;

    Comparator<Node> comparator;

    public AStar() {
        openList = new LinkedList<>();
        closedList = new HashSet<>();
        comparator = (a, b) -> Float.compare(a.g + a.h, b.g + b.h);
    }

    private void neighborVisitor(Node parent, T n) {
        Node node = new Node(n, parent);
        if (closedList.contains(node)) return;
        int index = openList.indexOf(node);
        if (index > -1) {
            Node old = openList.get(index);
            if (old.g > node.g) {
                old.setParent(parent);
            }
        } else {
            node.h = network.getH(n, finish);
            openList.add(node);
        }
    }

    public LinkedList<T> findPath(Network<T> network, T start, T finish) {
        this.network = network;

        LinkedList<T> path = new LinkedList<>();
        if (start == finish) {
            path.add(start);
            return path;
        }

        this.finish = finish;

        openList.clear();
        closedList.clear();
        Node startNode = new Node(start, null);
        startNode.h = network.getH(start, finish);

        openList.add(startNode);

        while (!openList.isEmpty()) {
            openList.sort(comparator);
            Node n = openList.poll();

            if (n.data.equals(finish)) {
                Node t = n;
                while (t != null) {
                    path.add(t.data);
                    t = t.parent;
                }

                Collections.reverse(path);
                return path;
            }

            closedList.add(n);
            network.visitNeighbors(n.data, x -> neighborVisitor(n, x));
        }

        return null;
    }
}
