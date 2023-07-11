package com.expo.security.service;

import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        ObjectMapper objectMapper = new ObjectMapper();
        List<UserDTO> userDetailsArray = new ArrayList<>();

        for (User user : users) {
            UserDTO userDetails=new UserDTO(user.getId(),user.getFirstname(),user.getLastname(),user.getEmail(),user.getRole());

            userDetailsArray.add(userDetails);
        }

        return userDetailsArray;
    }



}
