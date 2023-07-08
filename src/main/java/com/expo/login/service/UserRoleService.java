package com.expo.login.service;

import com.expo.login.model.UserRole;
import com.expo.login.repo.UserRoleRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public UserRole createUserRole(UserRole userRole) {

        return userRoleRepository.save(userRole);
    }
}
