package com.example.client;

import javax.swing.SwingUtilities;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.client.userinterface.UserInterface;

public class ClientApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.example.client");

        SwingUtilities.invokeLater(() -> {
            UserInterface editor = context.getBean(UserInterface.class);
        });
    }
}