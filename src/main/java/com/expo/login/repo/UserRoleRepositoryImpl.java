package com.expo.login.repo;


import com.expo.login.model.UserRole;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleRepositoryImpl extends SimpleJpaRepository<UserRole, Long> implements UserRoleRepository {
    public UserRoleRepositoryImpl(EntityManager entityManager) {
        super(UserRole.class, entityManager);
    }

}