package com.example.client.handler;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Scanner;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import java.lang.reflect.Type;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import com.example.client.model.*;
import com.example.client.userinterface.UserInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class WebSocketHandler {
    static StompSession stompSession;
    WebSocketStompClient stompClient;
    static UserInterface userInterface;

    public WebSocketHandler(UserInterface userInterface) {
        this.userInterface = userInterface;
        connectToWebSocket();
    }

    public void connectToWebSocket() {
        try {    
            List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);
            stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            String url = "ws://localhost:8080/ws";
            this.stompSession = stompClient.connect(url, sessionHandler).get();
            System.out.println("Websocket connection established");
        } catch (Exception e) {
            System.out.println("Websocket connection failed");
        }
    }

    public static void insert(String documentId, String userId, String positionId, ArrayList<Node> nodes) { 
        TransferData data = new TransferData(positionId, nodes);
        stompSession.send("/app/insert/" + documentId + "/" + userId, data);
    }    

    public static void delete(String documentId, String userId, String positionId) { 
        TransferData data = new TransferData(positionId, null);
        stompSession.send("/app/delete/" + documentId + "/" + userId, data); 
    }

    public static void undoRedo(String documentId, String userId, ArrayList<Node> nodes) { 
        TransferData data = new TransferData("NA", nodes);
        stompSession.send("/app/undo/redo/" + documentId + "/" + userId, data); 
    }

    public static void comingInsert(String documentId, String id) {
        stompSession.subscribe("/topic/coming/insert/" + documentId + "/" + id, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TransferData.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TransferData data = (TransferData) payload;
                userInterface.insertExternal(data);
            }
        });
    }

    public static void comingDelete(String documentId, String id) { 
        stompSession.subscribe("/topic/coming/delete/" + documentId + "/" + id, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TransferData.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TransferData data = (TransferData) payload;
                userInterface.deleteExternal(data);
            }
        });
    }

    public static void comingUndoRedo(String documentId, String id) { 
        stompSession.subscribe("/topic/coming/undo/redo/" + documentId + "/" + id, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TransferData.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TransferData data = (TransferData) payload;
                userInterface.undoRedoExternal(data);
            }
        });
    }

    public void close() {
        try {
            stompSession.disconnect();
        } catch (Exception e) {
        }
    }
}

class MyStompSessionHandler extends StompSessionHandlerAdapter {
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.err.println("An error occurred: " + exception.getMessage());
    }
}