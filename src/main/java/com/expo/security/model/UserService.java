package com.expo.security.model;

import com.expo.security.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public ArrayNode getAllUsers() {
        List<User> users = userRepository.findAll();
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode userDetailsArray = objectMapper.createArrayNode();

        for (User user : users) {
            ObjectNode userDetails = objectMapper.createObjectNode();
            userDetails.put("firstname", user.getFirstname());
            userDetails.put("lastname", user.getLastname());
            userDetails.put("role", user.getRole().toString());
            userDetails.put("email", user.getEmail());

            userDetailsArray.add(userDetails);
        }

        return userDetailsArray;
    }




}
