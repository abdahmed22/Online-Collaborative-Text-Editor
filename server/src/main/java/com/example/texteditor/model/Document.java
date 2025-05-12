package com.example.texteditor.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Document {
    private final String documentId;
    private final String editorCode;
    private final String viewerCode;
    private final User owner;
    private Map<String, User> users;
    private String text;
    private CRDT tree;

    public Document(String documentId, String editorCode, String viewerCode, User owner, CRDT tree) {
        this.documentId = documentId;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;
        this.owner = owner;
        this.tree = tree;
        this.text = "";

        this.users = new ConcurrentHashMap<>();
        this.users.put(owner.getUserId(), owner);
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public String getViewerCode() {
        return viewerCode;
    }

    public User getOwner() {
        return owner;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, User> getEditors() {
        return users.entrySet().stream()
                .filter(entry -> entry.getValue().getUserRole() == UserRole.EDITOR)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, User> getViewers() {
        return users.entrySet().stream()
                .filter(entry -> entry.getValue().getUserRole() == UserRole.VIEWER)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String getDocumentText() {
        return text;
    }

    public void setDocumentText(String text) {
        this.text = text;
    }

    public CRDT getCRDT() {
        return tree;
    }
}