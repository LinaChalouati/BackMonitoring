package com.expo.security.controller;

import com.expo.security.repo.TokenRepository;
import com.expo.security.model.User;
import com.expo.security.repo.UserRepository;
import com.expo.security.model.UserService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserController(UserService userService, UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }
/*
    @GetMapping("/get_all_users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }*/


    @GetMapping("/users")
    public ResponseEntity<ArrayNode> getAllUsers() {
        ArrayNode userDetailsArray = userService.getAllUsers();
        return ResponseEntity.ok(userDetailsArray);
    }

    @DeleteMapping("/delete_users")
    public ResponseEntity<Boolean> deleteUser(@RequestParam String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isPresent()) {
            // Delete related tokens first
            this.tokenRepository.deleteById(user.get().getId());

            // Then delete the user
            this.userRepository.delete(user.get());

            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @PutMapping("/update_user")
    public ResponseEntity<Boolean> updateUser(@RequestParam String email, @RequestBody User updatedUser) {
        System.out.println("User Firstname: " + updatedUser.getFirstname());
        System.out.println("User Password: " + updatedUser.getPassword());
        System.out.println("User Email: " + updatedUser.getEmail());
        System.out.println("User Lastname: " + updatedUser.getLastname());
        System.out.println("User Role: " + updatedUser.getRole());
        System.out.println("Email: " + email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            userRepository.save(user);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }




}
