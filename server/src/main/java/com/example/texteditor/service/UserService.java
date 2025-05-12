package com.example.texteditor.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.example.texteditor.model.User;
import com.example.texteditor.model.UserRole;


@Service
public class UserService {
    Map<String, User> userMap = new ConcurrentHashMap<>();
    AtomicInteger newId = new AtomicInteger(1);

    public User createUser(String userName, String userColor) {
        String id = String.valueOf(newId.getAndIncrement());

        User user = new User(id, userName, userColor, UserRole.valueOf("NONE"));
        userMap.put(id, user);
        return user;
    }

    public User getUserById(String userId) {
        return userMap.get(userId);
    }

    public void printUser(String userId) {
        User user = userMap.get(userId);
        if (user != null) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Username: " + user.getUserName());
            System.out.println("Role: " + user.getUserRole());
        } else {
            System.out.println("User with ID " + userId + " not found.");
        }
    }

    public void printAllUsers() {
        if (userMap.isEmpty()) {
            System.out.println("No users available.");
            return;
        }

        System.out.println("All users:");
        for (User user : userMap.values()) {
            System.out.println("ID: " + user.getUserId() + ", Name: " + user.getUserName() + ", Role: " + user.getUserRole());
        }
    }
  
}