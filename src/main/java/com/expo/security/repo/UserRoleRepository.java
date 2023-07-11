package com.expo.security.repo;

import com.expo.security.model.User;
import com.expo.security.model.UserProjectRole;
import com.expo.security.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
 //   void deleteAll(List<UserRole> userRoles);

    @Override
    void deleteAll();
}