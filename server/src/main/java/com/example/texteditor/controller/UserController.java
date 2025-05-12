package com.example.texteditor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.texteditor.model.User;
import com.example.texteditor.service.UserService;


@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/create")
    public User createUser(@RequestBody String[] userData) {
        String userName = userData[0];
        String userColor = userData[1];

        User user = userService.createUser(userName, userColor);

        return user;
    }
}
