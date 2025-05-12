package com.example.texteditor.model;

public class MyList {
    private Node head;
    private Node tail;

    public void setHead(Node node) {this.head = node;}

    public void setTail(Node node) {this.tail = node;}

    public void add(Node node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
    }
}