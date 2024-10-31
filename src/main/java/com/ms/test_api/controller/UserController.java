package com.ms.test_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.model.UserSoccerField;
import com.ms.test_api.service.impl.UserServiceImpl;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public List<UserSoccerField> getAllUsers(){
        return userServiceImpl.getAllUsers();
    }

    @PostMapping
    public UserSoccerField registerUser(@RequestBody UserSoccerField user){
        return userServiceImpl.registerUser(user);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserSoccerField> getUserByUsername(@PathVariable String username){
        return userServiceImpl.getUserByUsername(username);
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserSoccerField> updateUser(@PathVariable String username, @RequestBody UserSoccerField user){
        return userServiceImpl.updateUserByUsername(username, user);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username){
        return userServiceImpl.deleteUser(username);
    }
    

}
