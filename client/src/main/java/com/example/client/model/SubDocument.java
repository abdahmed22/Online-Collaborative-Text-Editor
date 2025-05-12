package com.example.client.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SubDocument {
    private final String documentId;
    private final String editorCode;
    private final String viewerCode;
    private final User owner;
    private Map<String, User> users;
    private String text;

    public SubDocument(String documentId, String editorCode, String viewerCode, User owner) {
        this.documentId = documentId;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;
        this.owner = owner;
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

    public String getDocumentText() {
        return text;
    }

    public void setDocumentText(String text) {
        this.text = text;
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

    private void printUser(User user) {
        System.out.println("User ID: " + user.getUserId());
        System.out.println("Username: " + user.getUserName());
        System.out.println("Role: " + user.getUserRole());
    }

    public void printDocument() {
        System.out.println("===== Document Info =====");
        System.out.println("Document ID: " + this.documentId);
        System.out.println("Editor Code: " + this.editorCode);
        System.out.println("Viewer Code: " + this.viewerCode);

        System.out.println("--- Owner ---");
        printUser(this.owner);

        System.out.println("--- Editors ---");
        if (this.getEditors().isEmpty()) {
            System.out.println("No editors.");
        } else {
            for (User user : this.getEditors().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Viewers ---");
        if (this.getViewers().isEmpty()) {
            System.out.println("No viewers.");
        } else {
            for (User user : this.getViewers().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Users ---");
        if (this.getUsers().isEmpty()) {
            System.out.println("No viewers.");
        } else {
            for (User user : this.getUsers().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Text ---");
        System.out.println(this.text);

        System.out.println("=========================");
    }
}