package com.example.texteditor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.texteditor.model.Document;
import com.example.texteditor.model.User;
import com.example.texteditor.model.UserRole;
import com.example.texteditor.service.DocumentService;
import com.example.texteditor.service.UserService;

@RestController
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping("/document/create") 
    public Document createDocument(@RequestBody String userId) {
        User owner = userService.getUserById(userId);
        if (owner == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Document document = documentService.createDocument(owner);        
        documentService.addUserToDocument(document, owner, UserRole.valueOf("OWNER"));

        return document;
    }

    @PostMapping("/document/join")
    public Document joinDocument(@RequestBody String[] requestData) {
        String userId = requestData[0];
        String sessionCode = requestData[1];

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        String documentId = documentService.getDocumentIdBySessionCode(sessionCode);
        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid document ID");
        }

        UserRole userRole  = documentService.getUserRoleFromSessionCode(document, sessionCode);
        if (userRole == UserRole.NONE) {
            throw new IllegalArgumentException("Invalid session code.");
        }

        documentService.addUserToDocument(document, user, userRole);

        return document;
    }

    @PostMapping("/document/leave/{documentId}")
    public ResponseEntity<Void> leaveDocument(@PathVariable String documentId, @RequestBody String userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid document ID");
        }

        boolean userRemoved = documentService.removeUserFromDocument(document, userId);
        if (!userRemoved) {
            throw new IllegalArgumentException("User not found in the document.");
        }

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/document/viewercode/{documentId}")
    public String getViewerCode(@PathVariable String documentId) {
        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid document ID");
        }

        return documentService.getViewerCode(document);
    }

    @GetMapping("/document/editorcode/{documentId}")
    public String getEditorCode(@PathVariable String documentId) {
        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid document ID");
        }

        return documentService.getEditorCode(document);
    }
}
