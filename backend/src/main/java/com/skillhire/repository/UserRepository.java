package com.skillhire.repository;

import com.skillhire.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndEmailNotificationsEnabled(User.Role role, boolean emailNotificationsEnabled);

    List<User> findByRole(User.Role role);
}
