package com.example.texteditor.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.example.texteditor.model.CRDT;
import com.example.texteditor.model.Document;
import com.example.texteditor.model.TransferData;
import com.example.texteditor.model.User;
import com.example.texteditor.model.UserRole;

@Service
public class DocumentService {
    Map<String, Document> documentMap = new ConcurrentHashMap<>();
    AtomicInteger newId = new AtomicInteger(1);

    public Document createDocument(User owner) {
        String id = String.valueOf(newId.getAndIncrement());
        String editorCode = "editor" + UUID.randomUUID().toString();
        String viewerCode = "viewer" + UUID.randomUUID().toString();

        CRDT tree = new CRDT();
        Document document = new Document(id, editorCode, viewerCode, owner, tree);
        documentMap.put(id, document);
        return document;
    }

    public Document getDocumentById(String documentId) {
        return documentMap.get(documentId);
    }

    public String getDocumentIdBySessionCode(String sessionCode) {
        for (Map.Entry<String, Document> entry : documentMap.entrySet()) {
            Document document = entry.getValue();
            if (sessionCode.equals(document.getEditorCode()) || sessionCode.equals(document.getViewerCode())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ArrayList<String> getUsersOfDocument(String documentId) {
        Document document = documentMap.get(documentId);
        if (document == null) {
            return null;
        }
        ArrayList<String> users = new ArrayList<>();
        for (Map.Entry<String, User> entry : document.getUsers().entrySet()) {
            users.add(entry.getKey());
        }
        return users;
    }
    
    public UserRole getUserRoleFromSessionCode(Document document, String sessionCode) {
        if (sessionCode.equals(document.getEditorCode())) {
            return UserRole.EDITOR;
        } else if (sessionCode.equals(document.getViewerCode())) {
            return UserRole.VIEWER;
        } else {
            return UserRole.NONE;
        }
    }

    public Document addUserToDocument(Document document, User user, UserRole userRole) {
        user.setUserRole(userRole);
        document.getUsers().put(user.getUserId(), user);
        document.setDocumentText(document.getCRDT().getDocumentAsString());

        return document;
    }

    public boolean removeUserFromDocument(Document document, String userId) {
        User removedUser = document.getUsers().remove(userId);
        return removedUser != null;
    }

    public String getViewerCode(Document document) {
        return document.getViewerCode();
    }

    public String getEditorCode(Document document) {
        return document.getEditorCode();
    }

    public boolean insertInCRDT(String documentId, TransferData data) {
        Document document = documentMap.get(documentId);
        if (document == null) {
            return false;
        }
        CRDT tree = document.getCRDT();
        tree.getCursor().setCurrent(tree.getMap().get(data.getPositionId()));
        tree.insertAndTraverse(data.getNodes());
        return true;
    }

    public boolean deleteFromCRDT(String documentId, String positionId) {
        Document document = documentMap.get(documentId);
        if (document == null) {
            return false;
        }
        CRDT tree = document.getCRDT();
        tree.getCursor().setCurrent(tree.getMap().get(positionId));
        tree.deleteAndTraverse(1);
        return true;
    }

    public boolean undoRedoFromCRDT(String documentId, TransferData data) {
        Document document = documentMap.get(documentId);
        if (document == null) {
            return false;
        }
        CRDT tree = document.getCRDT();
        tree.undoRedoExternalAndTraverse(data.getNodes());
        return true;
    }

    private void printUser(User user) {
        System.out.println("User ID: " + user.getUserId());
        System.out.println("Username: " + user.getUserName());
        System.out.println("Role: " + user.getUserRole());
    }

    public void printDocument(String documentId) {
        Document document = documentMap.get(documentId);
        if (document == null) {
            System.out.println("Document with ID " + documentId + " not found.");
            return;
        }

        System.out.println("===== Document Info =====");
        System.out.println("Document ID: " + documentId);
        System.out.println("Editor Code: " + document.getEditorCode());
        System.out.println("Viewer Code: " + document.getViewerCode());

        System.out.println("--- Owner ---");
        printUser(document.getOwner());

        System.out.println("--- Editors ---");
        if (document.getEditors().isEmpty()) {
            System.out.println("No editors.");
        } else {
            for (User user : document.getEditors().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Viewers ---");
        if (document.getViewers().isEmpty()) {
            System.out.println("No viewers.");
        } else {
            for (User user : document.getViewers().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Users ---");
        if (document.getUsers().isEmpty()) {
            System.out.println("No viewers.");
        } else {
            for (User user : document.getUsers().values()) {
                printUser(user);
            }
        }

        System.out.println("--- Tree ---");
        document.getCRDT().printTree();


        System.out.println("--- Text ---");
        System.out.println(document.getDocumentText());

        System.out.println("=========================");
    }
}
