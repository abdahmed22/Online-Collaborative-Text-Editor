package com.example.texteditor.controller;

import java.util.ArrayList;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.texteditor.model.TransferData;
import com.example.texteditor.service.DocumentService;
import com.example.texteditor.service.UserService;


@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final DocumentService documentService;
    private final UserService userService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, DocumentService documentService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.documentService = documentService;
        this.userService = userService;
    }

    @MessageMapping("/insert/{documentId}/{userId}")
    public void insert(@DestinationVariable String documentId, @DestinationVariable String userId, TransferData data) {
        documentService.insertInCRDT(documentId, data);
        ArrayList<String> users = documentService.getUsersOfDocument(documentId);
        users.removeIf(id -> id.equals(userId));
        for (String id : users) {
            messagingTemplate.convertAndSend("/topic/coming/insert/" + documentId + "/" + id, data);
        }
    }

    @MessageMapping("/delete/{documentId}/{userId}")
    public void delete(@DestinationVariable String documentId, @DestinationVariable String userId, TransferData data) {
        documentService.deleteFromCRDT(documentId, data.getPositionId());
        ArrayList<String> users = documentService.getUsersOfDocument(documentId);
        users.removeIf(id -> id.equals(userId));
        for (String id : users) {
            messagingTemplate.convertAndSend("/topic/coming/delete/" + documentId + "/" + id, data);
        }
    }

    @MessageMapping("/undo/redo/{documentId}/{userId}")
    public void undoRedo(@DestinationVariable String documentId, @DestinationVariable String userId, TransferData data) {
        documentService.undoRedoFromCRDT(documentId, data);
        ArrayList<String> users = documentService.getUsersOfDocument(documentId);
        users.removeIf(id -> id.equals(userId));
        for (String id : users) {
            messagingTemplate.convertAndSend("/topic/coming/undo/redo/" + documentId + "/" + id, data);
        }
    }
}