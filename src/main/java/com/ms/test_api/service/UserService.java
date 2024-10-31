package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ms.test_api.model.UserSoccerField;

public interface UserService {

    UserDetailsService userDetailsService();

    List<UserSoccerField> getAllUsers();

    UserSoccerField registerUser(UserSoccerField user);

    ResponseEntity<UserSoccerField> getUserByUsername(String username);

    ResponseEntity<UserSoccerField> updateUserByUsername(String username, UserSoccerField user);

    ResponseEntity<?> deleteUser(String username);

}
