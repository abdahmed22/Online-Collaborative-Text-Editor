package com.example.client.model;

import java.time.Instant;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class, 
    property = "id")
public class Node implements Comparable<Node> {
    private int userId;
    private Instant timestamp;
    private String id;
    private char data;
    private boolean deleted;
    private Node prev;
    private Node next;
    private static Instant lastInstant = Instant.now();
    private TreeSet<Node> children;

    public Node() {
        this.userId = 0;
        this.timestamp = Instant.now();
        this.id = "";
        this.data = '\0';
        this.deleted = false;
        this.prev = null;
        this.next = null;
        this.children = new TreeSet<>();
    }

    public Node(int userId, char data, boolean virtual) {
        this.userId = userId;
        Instant temp = Instant.now();
        while (temp.compareTo(lastInstant) <= 0) {
            temp = temp.plusNanos(1);
        }
        this.timestamp = temp;
        Node.lastInstant = this.timestamp;

        if (virtual) {
            this.id = userId + ",";
        } else {
            this.id = userId + "," + timestamp;
        }

        this.data = data;
        this.deleted = false;
        this.prev = null;
        this.next = null;
        this.children = new TreeSet<>();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public char getData() {
        return this.data;
    }

    public void setData(char data) {
        this.data = data;
    }

    public Node getPrev() {
        return this.prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return this.next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public TreeSet<Node> getChildren() {
        return this.children;
    }

    public void setChildren(TreeSet<Node> children) {
        this.children = children;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    @Override
    public int compareTo(Node other) {
        return other.timestamp.compareTo(this.timestamp);
    }
}
