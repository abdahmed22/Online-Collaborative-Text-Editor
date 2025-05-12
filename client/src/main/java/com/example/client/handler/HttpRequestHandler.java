package com.example.client.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.client.model.SubDocument;
import com.example.client.model.User;

@RestController
public class HttpRequestHandler {
    private static final RestTemplate restTemplate= new RestTemplate();
    private static final String baseUrl = "http://localhost:8080/";

    public static User createUser(String userName, String userColor) {
        String url = baseUrl + "user/create";
        try {
            String[] requestData = {userName , userColor};
            User user = restTemplate.postForObject(url, requestData, User.class);
            return user;
        } catch (Exception e) {
            System.out.println("Failed to create user: " + e.getMessage());
            return null;
        }
    }

    public static SubDocument createDocument(String userId) {                          
        String url = baseUrl + "document/create";
        try {
            SubDocument document = restTemplate.postForObject(url, userId, SubDocument.class);
            return document;
        } catch (Exception e) {
            System.out.println("Failed to create document: " + e.getMessage());
            return null;
        }
    }

    public static SubDocument joinDocument(String userId, String sessionCode) {
        String url = baseUrl + "document/join";
        try {
            String[] requestData = {userId , sessionCode};
            SubDocument response = restTemplate.postForObject(url, requestData, SubDocument.class);
            return response;
        } catch (Exception e) {
            System.out.println("Failed to join document: " + e.getMessage());
            return null;
        }
    }

    public static void leaveDocument(String userId, String documentId) {
        String url = baseUrl + "document/leave/" + documentId;
        try {
            restTemplate.postForObject(url, userId, Void.class);
        } catch (Exception e) {
            System.out.println("Failed to join document: " + e.getMessage());
        }
    }

    public static String getViewerCode(String documentId) {                              
        String url = baseUrl + "document/viewercode/" + documentId;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Failed to get viewer code: " + e.getMessage());
            return null;
        }
    }

    public static String getEditorCode(String documentId) {                        
        String url = baseUrl + "document/editorcode/" + documentId;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Failed to get editor code: " + e.getMessage());
            return null;
        }
    }
}