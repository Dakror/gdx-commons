package de.dakror.common.libgdx.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

public class AStar {
    public static abstract class Node {
        private Node parent;
        private float g;

        public float getG() {
            return g;
        }

        public void setParent(Node parent) {
            this.parent = parent;
            if (parent == null) g = 0;
            else g = parent.getG() + parent.getEdgeLengthTo(this);
        }

        public Node getParent() {
            return parent;
        }

        public float getF(Node target) {
            return g + getH(target);
        }

        public abstract float getH(Node target);

        public abstract int hashCode();

        public abstract float getEdgeLengthTo(Node neighbor);
        
        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass()) && hashCode() == obj.hashCode();
        }

        public abstract void visitNeighbors(Consumer<Node> visitor);
    }

    LinkedList<Node> openList;
    HashSet<Node> closedList;
    Node finish;

    Comparator<Node> comparator;
    
    public AStar() {
        openList = new LinkedList<>();
        closedList = new HashSet<>();
        comparator = (a, b) -> Float.compare(a.getF(finish), b.getF(finish));
    }

    private void neighborVisitor(Node parent, Node n) {
        if (closedList.contains(n)) return;
        int index = openList.indexOf(n);
        if (index > -1) {
            Node old = openList.get(index);
            float newG = parent.getG() + parent.getEdgeLengthTo(n);
            if(old.getG() > newG) {
                old.setParent(parent);
            }
        } else {
            n.setParent(parent);
            openList.add(n);
        }
    }

    public LinkedList<Node> findPath(Node start, Node finish) {
        LinkedList<Node> path = new LinkedList<>();
        if (start == finish) {
            path.add(start);
            return path;
        }

        openList.clear();
        closedList.clear();
        this.finish = finish;

        openList.add(start);

        while (!openList.isEmpty()) {
            openList.sort(comparator);
            Node n = openList.poll();
            
            if(n.equals(finish)) {
                Node t = n;
                while(t != null) {
                    path.add(t);
                    t = t.getParent();
                }
                
                Collections.reverse(path);
                return path;
            }
            
            closedList.add(n);
            n.visitNeighbors(x -> neighborVisitor(n, x));
        }

        return null;
    }
}
