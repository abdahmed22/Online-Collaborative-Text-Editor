package com.example.texteditor.model;

public class Cursor {
    private Node current;
    private final Node virtualNode;

    public Cursor(Node node) {
        current = node;
        virtualNode = node;
    }

    public void setCurrent(Node node) {
        current = node;
    }

    public Node getCurrent() {
        return this.current;
    }

    public void moveLeft() {
        if (current != virtualNode) current = current.getPrev();
    }

    public void moveRight() {
        if (current.getNext() != null) current = current.getNext();
    }
}
